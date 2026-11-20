import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { PropertyAsset, PropertyPayment, PropertyService } from './property.service';

@Component({
  selector: 'app-property',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './property.component.html'
})
export class PropertyComponent implements OnInit {
  private fb = inject(FormBuilder);
  private propertyService = inject(PropertyService);
  private auth = inject(AuthService);

  properties = signal<PropertyAsset[]>([]);
  payments = signal<PropertyPayment[]>([]);
  loading = signal(true);
  submittingProperty = signal(false);
  showPropertyForm = signal(false);
  scheduleFormOpenFor = signal<number | null>(null);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  propertyForm = this.fb.group({
    name: ['', Validators.required],
    address: [''],
    manager: ['']
  });

  scheduleForm = this.fb.group({
    paymentType: ['Rent', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    dueDate: ['', Validators.required]
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.propertyService.listProperties().subscribe((properties) => {
      this.properties.set(properties);
      this.propertyService.listPayments().subscribe((payments) => {
        this.payments.set(payments);
        this.loading.set(false);
      });
    });
  }

  submitProperty() {
    if (this.propertyForm.invalid) {
      this.propertyForm.markAllAsTouched();
      return;
    }
    this.submittingProperty.set(true);
    this.propertyService.addProperty(this.propertyForm.value as any).subscribe({
      next: () => {
        this.submittingProperty.set(false);
        this.propertyForm.reset();
        this.showPropertyForm.set(false);
        this.successMessage.set('Property added.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submittingProperty.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not add property.');
      }
    });
  }

  toggleScheduleForm(propertyId: number) {
    this.scheduleFormOpenFor.set(this.scheduleFormOpenFor() === propertyId ? null : propertyId);
    this.scheduleForm.reset({ paymentType: 'Rent' });
  }

  schedulePayment(propertyId: number) {
    if (this.scheduleForm.invalid) {
      this.scheduleForm.markAllAsTouched();
      return;
    }
    this.propertyService.schedulePayment(propertyId, this.scheduleForm.value as any).subscribe({
      next: () => {
        this.scheduleFormOpenFor.set(null);
        this.successMessage.set('Payment scheduled.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not schedule payment.')
    });
  }

  pay(paymentId: number) {
    this.propertyService.pay(paymentId).subscribe({
      next: (p) => {
        this.successMessage.set(`Payment for ${p.propertyName} submitted (${p.status}).`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not pay.')
    });
  }
}

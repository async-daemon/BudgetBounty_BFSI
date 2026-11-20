import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { InsurancePolicy, InsuranceService } from './insurance.service';

@Component({
  selector: 'app-insurance',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './insurance.component.html'
})
export class InsuranceComponent implements OnInit {
  private fb = inject(FormBuilder);
  private insuranceService = inject(InsuranceService);
  private auth = inject(AuthService);

  policies = signal<InsurancePolicy[]>([]);
  loading = signal(true);
  submitting = signal(false);
  showForm = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    provider: ['', Validators.required],
    policyNumber: ['', Validators.required],
    policyType: ['Health'],
    premiumAmount: [null as number | null, [Validators.required, Validators.min(1)]],
    dueDate: ['', Validators.required]
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.insuranceService.list().subscribe((data) => {
      this.policies.set(data);
      this.loading.set(false);
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.insuranceService.create(this.form.value as any).subscribe({
      next: () => {
        this.submitting.set(false);
        this.form.reset({ policyType: 'Health' });
        this.showForm.set(false);
        this.successMessage.set('Policy added.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not add policy.');
      }
    });
  }

  pay(id: number) {
    this.insuranceService.pay(id).subscribe({
      next: (p) => {
        this.successMessage.set(`Premium for ${p.provider} paid.`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not pay premium.')
    });
  }
}

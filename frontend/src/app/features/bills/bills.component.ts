import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Bill, BillsService } from './bills.service';

@Component({
  selector: 'app-bills',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './bills.component.html'
})
export class BillsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private billsService = inject(BillsService);
  private auth = inject(AuthService);

  bills = signal<Bill[]>([]);
  loading = signal(true);
  submitting = signal(false);
  showForm = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    name: ['', Validators.required],
    category: [''],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    dueDate: ['', Validators.required],
    recurring: [false],
    frequency: ['monthly'],
    autopay: [false]
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.billsService.list().subscribe((data) => {
      this.bills.set(data);
      this.loading.set(false);
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.billsService.create(this.form.value as any).subscribe({
      next: () => {
        this.submitting.set(false);
        this.form.reset({ recurring: false, autopay: false, frequency: 'monthly' });
        this.showForm.set(false);
        this.successMessage.set('Bill added.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not add bill.');
      }
    });
  }

  pay(id: number) {
    this.billsService.pay(id).subscribe({
      next: (b) => {
        this.successMessage.set(`${b.name} paid. +${b.rewardPointsEarned} reward points!`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not pay bill.')
    });
  }

  cancel(id: number) {
    this.billsService.cancel(id).subscribe({
      next: () => this.refresh(),
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not cancel bill.')
    });
  }
}

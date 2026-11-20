import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Transaction, TransfersService } from './transfers.service';

@Component({
  selector: 'app-transfers',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './transfers.component.html'
})
export class TransfersComponent implements OnInit {
  private fb = inject(FormBuilder);
  private transfersService = inject(TransfersService);
  private auth = inject(AuthService);

  transactions = signal<Transaction[]>([]);
  loading = signal(true);
  submitting = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);
  showForm = signal(false);

  form = this.fb.group({
    method: ['UPI', Validators.required],
    recipientName: ['', Validators.required],
    recipientHandle: ['', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    note: ['']
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.transfersService.list().subscribe((data) => {
      this.transactions.set(data);
      this.loading.set(false);
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.errorMessage.set(null);
    this.transfersService.create(this.form.value as any).subscribe({
      next: (tx) => {
        this.submitting.set(false);
        this.successMessage.set(`₹${tx.amount.toLocaleString('en-IN')} sent to ${tx.recipientName}. You earned ${tx.rewardPointsEarned} points!`);
        this.form.reset({ method: 'UPI' });
        this.showForm.set(false);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 5000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Transfer failed.');
      }
    });
  }
}

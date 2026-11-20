import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Budget, BudgetsService } from './budgets.service';

@Component({
  selector: 'app-budgets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './budgets.component.html'
})
export class BudgetsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private budgetsService = inject(BudgetsService);
  private auth = inject(AuthService);

  budgets = signal<Budget[]>([]);
  loading = signal(true);
  submitting = signal(false);
  showForm = signal(false);
  expenseFormOpenFor = signal<number | null>(null);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  form = this.fb.group({
    category: ['', Validators.required],
    monthlyLimit: [null as number | null, [Validators.required, Validators.min(1)]]
  });

  expenseForm = this.fb.group({
    amount: [null as number | null, [Validators.required, Validators.min(1)]],
    description: ['']
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.budgetsService.list().subscribe((data) => {
      this.budgets.set(data);
      this.loading.set(false);
    });
  }

  submit() {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.submitting.set(true);
    this.budgetsService.create(this.form.value as any).subscribe({
      next: () => {
        this.submitting.set(false);
        this.form.reset();
        this.showForm.set(false);
        this.successMessage.set('Budget created.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submitting.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not create budget.');
      }
    });
  }

  toggleExpenseForm(id: number) {
    this.expenseFormOpenFor.set(this.expenseFormOpenFor() === id ? null : id);
    this.expenseForm.reset();
  }

  logExpense(id: number) {
    if (this.expenseForm.invalid) {
      this.expenseForm.markAllAsTouched();
      return;
    }
    this.budgetsService.logExpense(id, this.expenseForm.value as any).subscribe({
      next: (b) => {
        this.successMessage.set(`Logged ₹${b.spentAmount.toLocaleString('en-IN')} spent in ${b.category}.`);
        this.expenseFormOpenFor.set(null);
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not log expense.')
    });
  }

  closeBudget(id: number) {
    this.budgetsService.close(id).subscribe({
      next: (b) => {
        this.successMessage.set(`Period closed. ${b.rewardPointsEarned} reward points earned.`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not close budget.')
    });
  }

  deleteBudget(id: number) {
    this.budgetsService.delete(id).subscribe({
      next: () => this.refresh(),
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not delete budget.')
    });
  }
}

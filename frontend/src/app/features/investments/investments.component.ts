import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { Investment, InvestmentGoal, InvestmentsService } from './investments.service';

@Component({
  selector: 'app-investments',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './investments.component.html'
})
export class InvestmentsComponent implements OnInit {
  private fb = inject(FormBuilder);
  private investmentsService = inject(InvestmentsService);
  private auth = inject(AuthService);

  goals = signal<InvestmentGoal[]>([]);
  investments = signal<Investment[]>([]);
  loading = signal(true);
  submittingGoal = signal(false);
  submittingInvestment = signal(false);
  showGoalForm = signal(false);
  showInvestmentForm = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  goalForm = this.fb.group({
    name: ['', Validators.required],
    targetAmount: [null as number | null, [Validators.required, Validators.min(1)]],
    targetDate: ['']
  });

  investmentForm = this.fb.group({
    name: ['', Validators.required],
    assetType: ['Mutual Fund'],
    investedAmount: [null as number | null, [Validators.required, Validators.min(1)]],
    currentValue: [null as number | null, [Validators.required, Validators.min(0)]],
    goalId: [null as number | null]
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.investmentsService.listGoals().subscribe((goals) => {
      this.goals.set(goals);
      this.investmentsService.list().subscribe((investments) => {
        this.investments.set(investments);
        this.loading.set(false);
      });
    });
  }

  submitGoal() {
    if (this.goalForm.invalid) {
      this.goalForm.markAllAsTouched();
      return;
    }
    this.submittingGoal.set(true);
    this.investmentsService.createGoal(this.goalForm.value as any).subscribe({
      next: () => {
        this.submittingGoal.set(false);
        this.goalForm.reset();
        this.showGoalForm.set(false);
        this.successMessage.set('Investment goal created.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submittingGoal.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not create goal.');
      }
    });
  }

  submitInvestment() {
    if (this.investmentForm.invalid) {
      this.investmentForm.markAllAsTouched();
      return;
    }
    this.submittingInvestment.set(true);
    this.investmentsService.createInvestment(this.investmentForm.value as any).subscribe({
      next: (inv) => {
        this.submittingInvestment.set(false);
        this.investmentForm.reset({ assetType: 'Mutual Fund' });
        this.showInvestmentForm.set(false);
        this.successMessage.set(`${inv.name} added to your portfolio.`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submittingInvestment.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not add investment.');
      }
    });
  }
}

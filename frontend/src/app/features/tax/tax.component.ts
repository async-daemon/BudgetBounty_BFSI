import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../core/services/auth.service';
import { TaxCalculation, TaxRecord, TaxService } from './tax.service';

@Component({
  selector: 'app-tax',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tax.component.html'
})
export class TaxComponent implements OnInit {
  private fb = inject(FormBuilder);
  private taxService = inject(TaxService);
  private auth = inject(AuthService);

  records = signal<TaxRecord[]>([]);
  calculations = signal<TaxCalculation[]>([]);
  result = signal<TaxCalculation | null>(null);
  loading = signal(true);
  calculating = signal(false);
  showRecordForm = signal(false);
  submittingRecord = signal(false);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  calcForm = this.fb.group({
    annualIncome: [null as number | null, [Validators.required, Validators.min(0)]],
    deductions: [0],
    regime: ['new']
  });

  recordForm = this.fb.group({
    assessmentYear: ['', Validators.required],
    taxType: ['Income Tax'],
    amountDue: [null as number | null, [Validators.required, Validators.min(1)]],
    dueDate: ['', Validators.required]
  });

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.taxService.listRecords().subscribe((records) => {
      this.records.set(records);
      this.taxService.recentCalculations().subscribe((calcs) => {
        this.calculations.set(calcs);
        this.loading.set(false);
      });
    });
  }

  calculate() {
    if (this.calcForm.invalid) {
      this.calcForm.markAllAsTouched();
      return;
    }
    this.calculating.set(true);
    this.taxService.calculate(this.calcForm.value as any).subscribe({
      next: (res) => {
        this.calculating.set(false);
        this.result.set(res);
        this.refresh();
      },
      error: (err) => {
        this.calculating.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not calculate tax.');
      }
    });
  }

  submitRecord() {
    if (this.recordForm.invalid) {
      this.recordForm.markAllAsTouched();
      return;
    }
    this.submittingRecord.set(true);
    this.taxService.addRecord(this.recordForm.value as any).subscribe({
      next: () => {
        this.submittingRecord.set(false);
        this.recordForm.reset({ taxType: 'Income Tax' });
        this.showRecordForm.set(false);
        this.successMessage.set('Tax record added.');
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submittingRecord.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not add tax record.');
      }
    });
  }

  payRecord(id: number) {
    this.taxService.payRecord(id).subscribe({
      next: (r) => {
        this.successMessage.set(`${r.taxType} for ${r.assessmentYear} paid. +${r.rewardPointsEarned} pts!`);
        this.refresh();
        this.auth.fetchCurrentUser().subscribe();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not pay.')
    });
  }
}

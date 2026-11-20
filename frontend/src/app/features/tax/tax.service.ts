import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface TaxRecord {
  id: number;
  assessmentYear: string;
  taxType: string;
  amountDue: number;
  dueDate: string;
  status: string;
  rewardPointsEarned: number;
  paidAt: string | null;
}

export interface TaxCalculation {
  id: number;
  annualIncome: number;
  regime: string;
  deductions: number;
  taxableIncome: number;
  taxPayable: number;
  cess: number;
  totalPayable: number;
}

export interface CalculateTaxRequest {
  annualIncome: number;
  deductions?: number;
  regime?: string;
}

export interface CreateTaxRecordRequest {
  assessmentYear: string;
  taxType?: string;
  amountDue: number;
  dueDate: string;
}

@Injectable({ providedIn: 'root' })
export class TaxService {
  private base = `${environment.apiBaseUrl}/tax`;

  constructor(private http: HttpClient) {}

  listRecords() {
    return this.http.get<TaxRecord[]>(`${this.base}/records`);
  }

  recentCalculations() {
    return this.http.get<TaxCalculation[]>(`${this.base}/calculations`);
  }

  calculate(payload: CalculateTaxRequest) {
    return this.http.post<TaxCalculation>(`${this.base}/calculator`, payload);
  }

  addRecord(payload: CreateTaxRecordRequest) {
    return this.http.post<TaxRecord>(`${this.base}/records`, payload);
  }

  payRecord(id: number) {
    return this.http.post<TaxRecord>(`${this.base}/records/${id}/pay`, {});
  }
}

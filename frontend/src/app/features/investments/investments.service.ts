import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface InvestmentGoal {
  id: number;
  name: string;
  targetAmount: number;
  targetDate: string | null;
  status: string;
  rewardPointsEarned: number;
  currentAmount: number;
  percentComplete: number;
}

export interface Investment {
  id: number;
  goalId: number | null;
  name: string;
  assetType: string;
  investedAmount: number;
  currentValue: number;
  gainLoss: number;
  gainLossPercent: number;
}

export interface CreateGoalRequest {
  name: string;
  targetAmount: number;
  targetDate?: string;
}

export interface CreateInvestmentRequest {
  name: string;
  assetType?: string;
  investedAmount: number;
  currentValue: number;
  goalId?: number | null;
}

@Injectable({ providedIn: 'root' })
export class InvestmentsService {
  private base = `${environment.apiBaseUrl}/investments`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Investment[]>(this.base);
  }

  listGoals() {
    return this.http.get<InvestmentGoal[]>(`${this.base}/goals`);
  }

  createGoal(payload: CreateGoalRequest) {
    return this.http.post<InvestmentGoal>(`${this.base}/goals`, payload);
  }

  createInvestment(payload: CreateInvestmentRequest) {
    return this.http.post<Investment>(this.base, payload);
  }
}

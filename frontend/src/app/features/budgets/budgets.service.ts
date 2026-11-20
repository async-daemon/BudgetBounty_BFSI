import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Budget {
  id: number;
  category: string;
  monthlyLimit: number;
  spentAmount: number;
  period: string;
  rewardPointsEarned: number;
  percentUsed: number;
  overBudget: boolean;
}

export interface CreateBudgetRequest {
  category: string;
  monthlyLimit: number;
}

export interface LogExpenseRequest {
  amount: number;
  description?: string;
}

@Injectable({ providedIn: 'root' })
export class BudgetsService {
  private base = `${environment.apiBaseUrl}/budgets`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Budget[]>(this.base);
  }

  create(payload: CreateBudgetRequest) {
    return this.http.post<Budget>(this.base, payload);
  }

  logExpense(id: number, payload: LogExpenseRequest) {
    return this.http.post<Budget>(`${this.base}/${id}/log-expense`, payload);
  }

  close(id: number) {
    return this.http.post<Budget>(`${this.base}/${id}/close`, {});
  }

  delete(id: number) {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Bill {
  id: number;
  name: string;
  category: string;
  amount: number;
  dueDate: string;
  recurring: boolean;
  frequency: string | null;
  autopay: boolean;
  status: string;
  rewardPointsEarned: number;
  paidAt: string | null;
  overdue: boolean;
}

export interface CreateBillRequest {
  name: string;
  category?: string;
  amount: number;
  dueDate: string;
  recurring: boolean;
  frequency?: string;
  autopay: boolean;
}

@Injectable({ providedIn: 'root' })
export class BillsService {
  private base = `${environment.apiBaseUrl}/bills`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Bill[]>(this.base);
  }

  create(payload: CreateBillRequest) {
    return this.http.post<Bill>(this.base, payload);
  }

  pay(id: number) {
    return this.http.post<Bill>(`${this.base}/${id}/pay`, {});
  }

  cancel(id: number) {
    return this.http.post<Bill>(`${this.base}/${id}/cancel`, {});
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface DashboardOverview {
  rewardPoints: number;
  totalTransferred: number;
  totalBudgetSpent: number;
  upcomingBillsCount: number;
  investmentCurrentValue: number;
  pendingPropertyPayments: number;
  activeInsurancePolicies: number;
  pendingTaxRecords: number;
  documentsCount: number;
  unreadNotifications: number;
}

export interface LinkedAccount {
  id: number;
  institutionName: string;
  accountType: string;
  accountNumberMasked: string | null;
  upiId: string | null;
  balance: number;
  status: string;
  linkedAt: string;
}

export interface NotificationItem {
  id: number;
  title: string;
  message: string;
  category: string;
  icon: string;
  read: boolean;
  createdAt: string;
}

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private base = `${environment.apiBaseUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  overview() {
    return this.http.get<DashboardOverview>(`${this.base}/overview`);
  }

  accounts() {
    return this.http.get<LinkedAccount[]>(`${this.base}/accounts`);
  }

  linkAccount(payload: { institutionName: string; accountType: string; accountNumberMasked?: string; upiId?: string }) {
    return this.http.post<LinkedAccount>(`${this.base}/accounts`, payload);
  }

  unlinkAccount(id: number) {
    return this.http.delete<void>(`${this.base}/accounts/${id}`);
  }

  notifications() {
    return this.http.get<NotificationItem[]>(`${this.base}/notifications`);
  }

  markNotificationsRead() {
    return this.http.post<void>(`${this.base}/notifications/mark-read`, {});
  }
}

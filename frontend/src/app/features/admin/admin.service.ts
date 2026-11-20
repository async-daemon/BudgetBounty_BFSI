import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface AdminUser {
  id: number;
  name: string;
  email: string;
  role: string;
  rewardPoints: number;
  createdAt: string;
}

export interface AdminRewardRule {
  id: number;
  module: string;
  label: string;
  pointsValue: number;
  description: string | null;
  active: boolean;
}

export interface CatalogItemAdmin {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  partnerName: string | null;
  pointsRequired: number;
  icon: string | null;
  active: boolean;
}

export interface CreateCatalogItemRequest {
  name: string;
  description?: string;
  category?: string;
  partnerName?: string;
  pointsRequired: number;
  icon?: string;
}

export interface AdminTransaction {
  id: number;
  referenceNo: string;
  method: string;
  recipientName: string;
  recipientHandle: string;
  amount: number;
  note: string | null;
  status: string;
  rewardPointsEarned: number;
  createdAt: string;
}

export interface AdminPropertyPayment {
  id: number;
  propertyId: number;
  propertyName: string;
  paymentType: string;
  amount: number;
  dueDate: string;
  status: string;
  rewardPointsEarned: number;
  paidAt: string | null;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private base = `${environment.apiBaseUrl}/admin`;

  constructor(private http: HttpClient) {}

  listUsers() {
    return this.http.get<AdminUser[]>(`${this.base}/users`);
  }

  listRewardRules() {
    return this.http.get<AdminRewardRule[]>(`${this.base}/reward-rules`);
  }

  updateRewardRule(module: string, pointsValue: number, active: boolean) {
    return this.http.put<AdminRewardRule>(`${this.base}/reward-rules/${module}`, { pointsValue, active });
  }

  listCatalog() {
    return this.http.get<CatalogItemAdmin[]>(`${this.base}/catalog`);
  }

  createCatalogItem(payload: CreateCatalogItemRequest) {
    return this.http.post<CatalogItemAdmin>(`${this.base}/catalog`, payload);
  }

  toggleCatalogItem(id: number, active: boolean) {
    return this.http.put<void>(`${this.base}/catalog/${id}/toggle?active=${active}`, {});
  }

  listTransactions() {
    return this.http.get<AdminTransaction[]>(`${this.base}/transactions`);
  }

  pendingPropertyApprovals() {
    return this.http.get<AdminPropertyPayment[]>(`${this.base}/property-approvals`);
  }

  approvePropertyPayment(id: number) {
    return this.http.post<AdminPropertyPayment>(`${this.base}/property-approvals/${id}/approve`, {});
  }

  rejectPropertyPayment(id: number) {
    return this.http.post<AdminPropertyPayment>(`${this.base}/property-approvals/${id}/reject`, {});
  }
}

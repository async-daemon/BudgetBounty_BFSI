import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface PropertyAsset {
  id: number;
  name: string;
  address: string | null;
  propertyManager: string | null;
}

export interface PropertyPayment {
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

export interface CreatePropertyRequest {
  name: string;
  address?: string;
  manager?: string;
}

export interface SchedulePaymentRequest {
  paymentType: string;
  amount: number;
  dueDate: string;
}

@Injectable({ providedIn: 'root' })
export class PropertyService {
  private base = `${environment.apiBaseUrl}/property`;

  constructor(private http: HttpClient) {}

  listProperties() {
    return this.http.get<PropertyAsset[]>(this.base);
  }

  listPayments() {
    return this.http.get<PropertyPayment[]>(`${this.base}/payments`);
  }

  addProperty(payload: CreatePropertyRequest) {
    return this.http.post<PropertyAsset>(this.base, payload);
  }

  schedulePayment(propertyId: number, payload: SchedulePaymentRequest) {
    return this.http.post<PropertyPayment>(`${this.base}/${propertyId}/schedule-payment`, payload);
  }

  pay(paymentId: number) {
    return this.http.post<PropertyPayment>(`${this.base}/payments/${paymentId}/pay`, {});
  }
}

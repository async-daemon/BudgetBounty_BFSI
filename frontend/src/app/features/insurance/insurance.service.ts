import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface InsurancePolicy {
  id: number;
  provider: string;
  policyNumber: string;
  policyType: string;
  premiumAmount: number;
  dueDate: string;
  status: string;
}

export interface CreatePolicyRequest {
  provider: string;
  policyNumber: string;
  policyType?: string;
  premiumAmount: number;
  dueDate: string;
}

@Injectable({ providedIn: 'root' })
export class InsuranceService {
  private base = `${environment.apiBaseUrl}/insurance`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<InsurancePolicy[]>(this.base);
  }

  create(payload: CreatePolicyRequest) {
    return this.http.post<InsurancePolicy>(this.base, payload);
  }

  pay(id: number) {
    return this.http.post<InsurancePolicy>(`${this.base}/${id}/pay`, {});
  }
}

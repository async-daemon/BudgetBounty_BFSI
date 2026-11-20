import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface Transaction {
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

export interface TransferRequest {
  method: string;
  recipientName: string;
  recipientHandle: string;
  amount: number;
  note?: string;
}

@Injectable({ providedIn: 'root' })
export class TransfersService {
  private base = `${environment.apiBaseUrl}/transfers`;

  constructor(private http: HttpClient) {}

  list() {
    return this.http.get<Transaction[]>(this.base);
  }

  create(payload: TransferRequest) {
    return this.http.post<Transaction>(this.base, payload);
  }
}

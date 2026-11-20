import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

export interface RewardCatalogItem {
  id: number;
  name: string;
  description: string | null;
  category: string | null;
  partnerName: string | null;
  pointsRequired: number;
  icon: string | null;
}

export interface RewardTransaction {
  id: number;
  points: number;
  entryType: string;
  sourceModule: string | null;
  description: string | null;
  createdAt: string;
}

export interface Redemption {
  id: number;
  itemName: string;
  pointsSpent: number;
  status: string;
  redeemedAt: string;
}

@Injectable({ providedIn: 'root' })
export class RewardsService {
  private base = `${environment.apiBaseUrl}/rewards`;

  constructor(private http: HttpClient) {}

  catalog() {
    return this.http.get<RewardCatalogItem[]>(`${this.base}/catalog`);
  }

  ledger() {
    return this.http.get<RewardTransaction[]>(`${this.base}/ledger`);
  }

  redemptions() {
    return this.http.get<Redemption[]>(`${this.base}/redemptions`);
  }

  redeem(itemId: number) {
    return this.http.post<Redemption>(`${this.base}/catalog/${itemId}/redeem`, {});
  }
}

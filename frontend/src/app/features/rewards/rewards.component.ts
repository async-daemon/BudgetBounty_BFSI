import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { Redemption, RewardCatalogItem, RewardTransaction, RewardsService } from './rewards.service';

@Component({
  selector: 'app-rewards',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './rewards.component.html'
})
export class RewardsComponent implements OnInit {
  private rewardsService = inject(RewardsService);
  auth = inject(AuthService);

  catalog = signal<RewardCatalogItem[]>([]);
  ledger = signal<RewardTransaction[]>([]);
  redemptions = signal<Redemption[]>([]);
  loading = signal(true);
  redeemingId = signal<number | null>(null);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  ngOnInit() {
    this.refresh();
  }

  refresh() {
    this.loading.set(true);
    this.rewardsService.catalog().subscribe((catalog) => {
      this.catalog.set(catalog);
      this.rewardsService.ledger().subscribe((ledger) => {
        this.ledger.set(ledger);
        this.rewardsService.redemptions().subscribe((redemptions) => {
          this.redemptions.set(redemptions);
          this.loading.set(false);
        });
      });
    });
  }

  redeem(item: RewardCatalogItem) {
    const points = this.auth.currentUser()?.rewardPoints ?? 0;
    if (points < item.pointsRequired) {
      this.errorMessage.set(`You need ${item.pointsRequired - points} more points to redeem ${item.name}.`);
      return;
    }
    this.redeemingId.set(item.id);
    this.rewardsService.redeem(item.id).subscribe({
      next: (r) => {
        this.redeemingId.set(null);
        this.successMessage.set(`Redeemed ${r.itemName} for ${r.pointsSpent} points!`);
        this.auth.fetchCurrentUser().subscribe();
        this.refresh();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.redeemingId.set(null);
        this.errorMessage.set(err?.error?.message || 'Could not redeem this item.');
      }
    });
  }
}

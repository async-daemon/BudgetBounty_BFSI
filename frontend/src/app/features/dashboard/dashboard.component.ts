import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/services/auth.service';
import { DashboardOverview, DashboardService, NotificationItem } from './dashboard.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  overview = signal<DashboardOverview | null>(null);
  notifications = signal<NotificationItem[]>([]);
  loading = signal(true);

  constructor(public auth: AuthService, private dashboardService: DashboardService) {}

  ngOnInit() {
    this.dashboardService.overview().subscribe((data) => {
      this.overview.set(data);
      this.loading.set(false);
    });
    this.dashboardService.notifications().subscribe((data) => this.notifications.set(data.slice(0, 5)));
  }

  formatCurrency(value: number | undefined): string {
    if (value === undefined || value === null) return '₹0';
    return '₹' + value.toLocaleString('en-IN', { maximumFractionDigits: 0 });
  }
}

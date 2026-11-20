import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-shell',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './shell.component.html'
})
export class ShellComponent {
  sidebarOpen = signal(false);

  navItems = [
    { path: '/dashboard', label: 'Dashboard', icon: 'fa-house' },
    { path: '/transfers', label: 'Money Transfers', icon: 'fa-money-bill-transfer' },
    { path: '/budgets', label: 'Cost Control', icon: 'fa-chart-pie' },
    { path: '/bills', label: 'Bill Payments', icon: 'fa-file-invoice' },
    { path: '/investments', label: 'Capital Companion', icon: 'fa-chart-line' },
    { path: '/property', label: 'Property Payment Pro', icon: 'fa-building' },
    { path: '/insurance', label: 'Insurance', icon: 'fa-shield-heart' },
    { path: '/tax', label: 'Tax Wise', icon: 'fa-calculator' },
    { path: '/locker', label: 'Digital Locker', icon: 'fa-lock' },
    { path: '/rewards', label: 'Rewards', icon: 'fa-gift' }
  ];

  constructor(public auth: AuthService) {}

  toggleSidebar() {
    this.sidebarOpen.update((v) => !v);
  }

  logout() {
    this.auth.logout();
  }
}

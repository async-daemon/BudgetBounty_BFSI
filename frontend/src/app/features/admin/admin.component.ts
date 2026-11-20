import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import {
  AdminPropertyPayment,
  AdminRewardRule,
  AdminService,
  AdminTransaction,
  AdminUser,
  CatalogItemAdmin
} from './admin.service';

type Tab = 'users' | 'rewardRules' | 'catalog' | 'transactions' | 'approvals';

@Component({
  selector: 'app-admin',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './admin.component.html'
})
export class AdminComponent implements OnInit {
  private fb = inject(FormBuilder);
  private adminService = inject(AdminService);

  activeTab = signal<Tab>('users');
  loading = signal(true);
  successMessage = signal<string | null>(null);
  errorMessage = signal<string | null>(null);

  users = signal<AdminUser[]>([]);
  rewardRules = signal<AdminRewardRule[]>([]);
  catalog = signal<CatalogItemAdmin[]>([]);
  transactions = signal<AdminTransaction[]>([]);
  approvals = signal<AdminPropertyPayment[]>([]);

  showCatalogForm = signal(false);
  submittingCatalog = signal(false);

  catalogForm = this.fb.group({
    name: ['', Validators.required],
    description: [''],
    category: ['General'],
    partnerName: [''],
    pointsRequired: [null as number | null, [Validators.required, Validators.min(1)]],
    icon: ['fa-gift']
  });

  ngOnInit() {
    this.refreshAll();
  }

  setTab(tab: Tab) {
    this.activeTab.set(tab);
  }

  refreshAll() {
    this.loading.set(true);
    this.adminService.listUsers().subscribe((users) => {
      this.users.set(users);
      this.adminService.listRewardRules().subscribe((rules) => {
        this.rewardRules.set(rules);
        this.adminService.listCatalog().subscribe((catalog) => {
          this.catalog.set(catalog);
          this.adminService.listTransactions().subscribe((transactions) => {
            this.transactions.set(transactions);
            this.adminService.pendingPropertyApprovals().subscribe((approvals) => {
              this.approvals.set(approvals);
              this.loading.set(false);
            });
          });
        });
      });
    });
  }

  updateRule(rule: AdminRewardRule, points: number) {
    this.adminService.updateRewardRule(rule.module, points, rule.active).subscribe({
      next: () => {
        this.successMessage.set(`${rule.label} updated to ${points} points.`);
        this.refreshAll();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not update rule.')
    });
  }

  toggleRuleActive(rule: AdminRewardRule) {
    this.adminService.updateRewardRule(rule.module, rule.pointsValue, !rule.active).subscribe({
      next: () => this.refreshAll(),
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not update rule.')
    });
  }

  submitCatalogItem() {
    if (this.catalogForm.invalid) {
      this.catalogForm.markAllAsTouched();
      return;
    }
    this.submittingCatalog.set(true);
    this.adminService.createCatalogItem(this.catalogForm.value as any).subscribe({
      next: () => {
        this.submittingCatalog.set(false);
        this.catalogForm.reset({ category: 'General', icon: 'fa-gift' });
        this.showCatalogForm.set(false);
        this.successMessage.set('Catalog item created.');
        this.refreshAll();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => {
        this.submittingCatalog.set(false);
        this.errorMessage.set(err?.error?.message || 'Could not create catalog item.');
      }
    });
  }

  toggleCatalogActive(item: CatalogItemAdmin) {
    this.adminService.toggleCatalogItem(item.id, !item.active).subscribe({
      next: () => this.refreshAll(),
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not toggle item.')
    });
  }

  approve(id: number) {
    this.adminService.approvePropertyPayment(id).subscribe({
      next: () => {
        this.successMessage.set('Payment approved.');
        this.refreshAll();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not approve.')
    });
  }

  reject(id: number) {
    this.adminService.rejectPropertyPayment(id).subscribe({
      next: () => {
        this.successMessage.set('Payment rejected.');
        this.refreshAll();
        setTimeout(() => this.successMessage.set(null), 4000);
      },
      error: (err) => this.errorMessage.set(err?.error?.message || 'Could not reject.')
    });
  }
}

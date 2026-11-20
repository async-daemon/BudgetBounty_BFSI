import { Routes } from '@angular/router';
import { authGuard, adminGuard } from './core/guards/auth.guard';
import { ShellComponent } from './shared/layout/shell.component';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./features/auth/login/login.component').then(m => m.LoginComponent) },
  { path: 'register', loadComponent: () => import('./features/auth/register/register.component').then(m => m.RegisterComponent) },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      { path: 'dashboard', loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'transfers', loadComponent: () => import('./features/transfers/transfers.component').then(m => m.TransfersComponent) },
      { path: 'budgets', loadComponent: () => import('./features/budgets/budgets.component').then(m => m.BudgetsComponent) },
      { path: 'bills', loadComponent: () => import('./features/bills/bills.component').then(m => m.BillsComponent) },
      { path: 'investments', loadComponent: () => import('./features/investments/investments.component').then(m => m.InvestmentsComponent) },
      { path: 'property', loadComponent: () => import('./features/property/property.component').then(m => m.PropertyComponent) },
      { path: 'insurance', loadComponent: () => import('./features/insurance/insurance.component').then(m => m.InsuranceComponent) },
      { path: 'tax', loadComponent: () => import('./features/tax/tax.component').then(m => m.TaxComponent) },
      { path: 'locker', loadComponent: () => import('./features/locker/locker.component').then(m => m.LockerComponent) },
      { path: 'rewards', loadComponent: () => import('./features/rewards/rewards.component').then(m => m.RewardsComponent) },
      { path: 'admin', canActivate: [adminGuard], loadComponent: () => import('./features/admin/admin.component').then(m => m.AdminComponent) },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },
  { path: '**', redirectTo: 'login' }
];

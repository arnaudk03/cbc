import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full'
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/auth/login.component').then((m) => m.LoginComponent)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then((m) => m.DashboardComponent)
  },
  {
    path: 'collectes',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/collectes/collecte.component').then((m) => m.CollecteComponent)
  },
  {
    path: 'paiements',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/paiements/paiement.component').then((m) => m.PaiementComponent)
  },
  {
    path: 'stocks',
    canActivate: [authGuard],
    loadComponent: () => import('./features/stocks/stock.component').then((m) => m.StockComponent)
  },
  {
    path: 'notifications',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/notifications/notification.component').then(
        (m) => m.NotificationComponent
      )
  },
  {
    path: 'referentiels',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/referentiels/referentiel.component').then(
        (m) => m.ReferentielComponent
      )
  },
  {
    path: '**',
    redirectTo: 'dashboard'
  }
];

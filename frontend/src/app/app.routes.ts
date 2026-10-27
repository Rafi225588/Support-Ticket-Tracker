import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', redirectTo: 'tickets', pathMatch: 'full' },
  {
    path: 'tickets',
    loadComponent: () =>
      import('./pages/ticket-list/ticket-list').then((m) => m.TicketList),
  },
  {
    path: 'tickets/new',
    loadComponent: () =>
      import('./pages/ticket-form/ticket-form').then((m) => m.TicketForm),
  },
  {
    path: 'tickets/:id/edit',
    loadComponent: () =>
      import('./pages/ticket-form/ticket-form').then((m) => m.TicketForm),
  },
  {
    path: 'tickets/:id',
    loadComponent: () =>
      import('./pages/ticket-detail/ticket-detail').then((m) => m.TicketDetail),
  },
  {
    path: 'users',
    loadComponent: () =>
      import('./pages/user-list/user-list').then((m) => m.UserList),
  },
  {
    path: '**',
    loadComponent: () =>
      import('./pages/not-found/not-found').then((m) => m.NotFound),
  },
];

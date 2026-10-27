import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { Ticket } from '../../models/ticket.model';
import { ROLES, Role } from '../../models/enums';

@Component({
  selector: 'app-user-list',
  imports: [FormsModule, RouterLink],
  templateUrl: './user-list.html',
  styleUrl: './user-list.css',
})
export class UserList implements OnInit {
  private userService = inject(UserService);

  roles = ROLES;

  users = signal<User[]>([]);
  loading = signal(true);
  saving = signal(false);
  error = signal('');
  success = signal('');

  // Form state
  showForm = signal(false);
  editingId: number | null = null;
  name = '';
  email = '';
  role: Role = 'CUSTOMER';

  // Kon user er ticket khola ache
  expandedUserId = signal<number | null>(null);
  userTickets = signal<Ticket[]>([]);
  ticketsLoading = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.userService.getAll().subscribe({
      next: (users) => {
        this.users.set(users);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.name = '';
    this.email = '';
    this.role = 'CUSTOMER';
    this.clearMessages();
    this.showForm.set(true);
  }

  openEdit(user: User): void {
    this.editingId = user.id;
    this.name = user.name;
    this.email = user.email;
    this.role = user.role;
    this.clearMessages();
    this.showForm.set(true);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingId = null;
  }

  save(): void {
    this.saving.set(true);
    this.clearMessages();

    const body = {
      name: this.name.trim(),
      email: this.email.trim(),
      role: this.role,
    };

    const isEdit = this.editingId !== null;
    const request$ = isEdit
      ? this.userService.update(this.editingId as number, body)
      : this.userService.create(body);

    request$.subscribe({
      next: () => {
        this.success.set(isEdit ? 'User updated' : 'User created');
        this.saving.set(false);
        this.showForm.set(false);
        this.editingId = null;
        this.load();
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  deleteUser(user: User): void {
    if (!confirm(`Delete user "${user.name}"?`)) return;
    this.clearMessages();

    this.userService.delete(user.id).subscribe({
      next: () => {
        this.success.set('User deleted');
        if (this.expandedUserId() === user.id) {
          this.expandedUserId.set(null);
        }
        this.load();
      },
      error: (err: HttpErrorResponse) => this.error.set(this.errorMessage(err)),
    });
  }

  toggleTickets(user: User): void {
    // Abar click korle bondho
    if (this.expandedUserId() === user.id) {
      this.expandedUserId.set(null);
      return;
    }

    this.expandedUserId.set(user.id);
    this.userTickets.set([]);
    this.ticketsLoading.set(true);

    this.userService.getTickets(user.id).subscribe({
      next: (tickets) => {
        this.userTickets.set(tickets);
        this.ticketsLoading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.ticketsLoading.set(false);
      },
    });
  }

  private clearMessages(): void {
    this.error.set('');
    this.success.set('');
  }

  private errorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Can not connect to the backend.';
    }
    return err.error?.message ?? 'Something went wrong';
  }
}

import { Component, OnInit, inject, signal } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketService } from '../../services/ticket.service';
import { UserService } from '../../services/user.service';
import { User } from '../../models/user.model';
import { PRIORITIES, Priority } from '../../models/enums';

@Component({
  selector: 'app-ticket-form',
  imports: [FormsModule, RouterLink],
  templateUrl: './ticket-form.html',
  styleUrl: './ticket-form.css',
})
export class TicketForm implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ticketService = inject(TicketService);
  private userService = inject(UserService);

  priorities = PRIORITIES;

  isEdit = false;
  ticketId: number | null = null;

  customers = signal<User[]>([]);
  loading = signal(false);
  saving = signal(false);
  error = signal('');

  // Form er value
  title = '';
  description = '';
  priority: Priority | '' = '';
  createdById: number | null = null;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    if (id) {
      // Edit mode: ticket load kore form bhoro
      this.isEdit = true;
      this.ticketId = Number(id);
      this.loadTicket(this.ticketId);
    } else {
      // Create mode: customer der list lagbe
      this.loadCustomers();
    }
  }

  private loadTicket(id: number): void {
    this.loading.set(true);
    this.ticketService.getById(id).subscribe({
      next: (ticket) => {
        this.title = ticket.title;
        this.description = ticket.description;
        this.priority = ticket.priority;
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  private loadCustomers(): void {
    this.userService.getAll().subscribe({
      next: (users) => this.customers.set(users.filter((u) => u.role === 'CUSTOMER')),
      error: (err: HttpErrorResponse) => this.error.set(this.errorMessage(err)),
    });
  }

  save(): void {
    this.saving.set(true);
    this.error.set('');

    const body = {
      title: this.title.trim(),
      description: this.description.trim(),
      priority: this.priority as Priority,
    };

    const request$ =
      this.isEdit && this.ticketId !== null
        ? this.ticketService.update(this.ticketId, body)
        : this.ticketService.create({ ...body, createdById: this.createdById as number });

    request$.subscribe({
      next: () => this.router.navigate(['/tickets']),
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.saving.set(false);
      },
    });
  }

  private errorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Can not connect in backend. check if the backend is running';
    }
    return err.error?.message ?? 'Something went wrong';
  }
}

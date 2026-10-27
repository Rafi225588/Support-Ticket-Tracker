import { Component, OnInit, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketService } from '../../services/ticket.service';
import { Ticket } from '../../models/ticket.model';
import { PRIORITIES, STATUSES, Priority, TicketStatus } from '../../models/enums';

@Component({
  selector: 'app-ticket-list',
  imports: [RouterLink, FormsModule, DatePipe],
  templateUrl: './ticket-list.html',
  styleUrl: './ticket-list.css',
})
export class TicketList implements OnInit {
  private ticketService = inject(TicketService);

  // Screen e ja dekhabe (signal)
  tickets = signal<Ticket[]>([]);
  page = signal(0);
  totalPages = signal(0);
  totalElements = signal(0);
  loading = signal(false);
  error = signal('');

  // Filter er dropdown list
  statuses = STATUSES;
  priorities = PRIORITIES;

  // Filter er current value
  status: TicketStatus | '' = '';
  priority: Priority | '' = '';
  search = '';
  size = 10;

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set('');

    this.ticketService
      .getTickets({
        status: this.status,
        priority: this.priority,
        search: this.search,
        page: this.page(),
        size: this.size,
        sort: 'createdAt,desc',
      })
      .subscribe({
        next: (res) => {
          this.tickets.set(res.content);
          this.totalPages.set(res.totalPages);
          this.totalElements.set(res.totalElements);
          this.loading.set(false);
        },
        error: (err: HttpErrorResponse) => {
          this.error.set(this.errorMessage(err));
          this.loading.set(false);
        },
      });
  }

  // Filter/search bodlale 1st page theke shuru
  applyFilters(): void {
    this.page.set(0);
    this.load();
  }

  clearFilters(): void {
    this.status = '';
    this.priority = '';
    this.search = '';
    this.applyFilters();
  }

  prevPage(): void {
    if (this.page() > 0) {
      this.page.update((p) => p - 1);
      this.load();
    }
  }

  nextPage(): void {
    if (this.page() < this.totalPages() - 1) {
      this.page.update((p) => p + 1);
      this.load();
    }
  }

  deleteTicket(ticket: Ticket): void {
    if (!confirm(`Delete ticket #${ticket.id} "${ticket.title}"?`)) {
      return;
    }

    this.ticketService.delete(ticket.id).subscribe({
      next: () => {
        // Page er shesh ticket delete korle ager page e jao
        if (this.tickets().length === 1 && this.page() > 0) {
          this.page.update((p) => p - 1);
        }
        this.load();
      },
      error: (err: HttpErrorResponse) => this.error.set(this.errorMessage(err)),
    });
  }

  private errorMessage(err: HttpErrorResponse): string {
    if (err.status === 0) {
      return 'Backend e connect kora jachche na. Backend chalu ache ki na dekho.';
    }
    return err.error?.message ?? 'Something went wrong';
  }
}

import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { TicketService } from '../../services/ticket.service';
import { Ticket } from '../../models/ticket.model';
import { STATUS_FLOW, TicketStatus } from '../../models/enums';

@Component({
  selector: 'app-ticket-detail',
  imports: [RouterLink, DatePipe],
  templateUrl: './ticket-detail.html',
  styleUrl: './ticket-detail.css',
})
export class TicketDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private ticketService = inject(TicketService);

  ticket = signal<Ticket | null>(null);
  loading = signal(true);
  changing = signal(false);
  error = signal('');
  success = signal('');

  // Current status theke kon kon status e jawa jay (auto update hoy)
  nextStatuses = computed<TicketStatus[]>(() => {
    const t = this.ticket();
    return t ? STATUS_FLOW[t.status] : [];
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.ticketService.getById(id).subscribe({
      next: (t) => {
        this.ticket.set(t);
        this.loading.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.loading.set(false);
      },
    });
  }

  changeStatus(newStatus: TicketStatus): void {
    const t = this.ticket();
    if (!t) return;

    this.changing.set(true);
    this.error.set('');
    this.success.set('');

    this.ticketService.changeStatus(t.id, { status: newStatus }).subscribe({
      next: (updated) => {
        this.ticket.set(updated);
        this.success.set(`Status changed to ${updated.status}`);
        this.changing.set(false);
      },
      error: (err: HttpErrorResponse) => {
        this.error.set(this.errorMessage(err));
        this.changing.set(false);
      },
    });
  }

  deleteTicket(): void {
    const t = this.ticket();
    if (!t) return;
    if (!confirm(`Delete ticket #${t.id} "${t.title}"?`)) return;

    this.ticketService.delete(t.id).subscribe({
      next: () => this.router.navigate(['/tickets']),
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

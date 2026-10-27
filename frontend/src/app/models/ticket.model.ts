import { Priority, TicketStatus } from './enums';
import { User } from './user.model';

// Backend theke ja ashe (TicketResponse)
export interface Ticket {
  id: number;
  title: string;
  description: string;
  status: TicketStatus;
  priority: Priority;
  createdBy: User;
  createdAt: string;
  updatedAt: string;
}

// Update (PUT) er jonno (TicketRequest)
export interface TicketRequest {
  title: string;
  description: string;
  priority: Priority;
}

// Create (POST) er jonno (CreateTicketRequest)
export interface CreateTicketRequest extends TicketRequest {
  createdById: number;
}

// Status change (PATCH) er jonno (TicketStatusRequest)
export interface TicketStatusRequest {
  status: TicketStatus;
}

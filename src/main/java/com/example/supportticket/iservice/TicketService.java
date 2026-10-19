package com.example.supportticket.iservice;

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.PageResponse;
import com.example.supportticket.dto.TicketRequest;
import com.example.supportticket.dto.TicketResponse;
import com.example.supportticket.dto.TicketStatusRequest;
import com.example.supportticket.enums.Priority;
import com.example.supportticket.enums.TicketStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    PageResponse<TicketResponse> getTickets(TicketStatus status, Priority priority, String search, Pageable pageable);

    TicketResponse getTicketById(Long id);

    TicketResponse updateTicket(Long id, TicketRequest request);

    TicketResponse changeStatus(Long id, TicketStatusRequest request);

    void deleteTicket(Long id);

    List<TicketResponse> getTicketsByUserId(Long userId);
}
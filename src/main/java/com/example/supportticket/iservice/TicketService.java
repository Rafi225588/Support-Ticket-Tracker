package com.example.supportticket.iservice;

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketRequest;
import com.example.supportticket.dto.TicketResponse;
import com.example.supportticket.dto.TicketStatusRequest;

import java.util.List;

public interface TicketService {

    TicketResponse createTicket(CreateTicketRequest request);

    TicketResponse getTicketById(Long id);

    TicketResponse updateTicket(Long id, TicketRequest request);

    TicketResponse changeStatus(Long id, TicketStatusRequest request);

    void deleteTicket(Long id);

    List<TicketResponse> getTicketsByUserId(Long userId);
}
package com.example.supportticket.service;

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketRequest;
import com.example.supportticket.dto.TicketResponse;
import com.example.supportticket.dto.TicketStatusRequest;
import com.example.supportticket.entity.Ticket;
import com.example.supportticket.entity.User;
import com.example.supportticket.enums.Role;
import com.example.supportticket.enums.TicketStatus;
import com.example.supportticket.exception.BadRequestException;
import com.example.supportticket.exception.ResourceNotFoundException;
import com.example.supportticket.iservice.TicketService;
import com.example.supportticket.repository.TicketRepository;
import com.example.supportticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public TicketServiceImpl(TicketRepository ticketRepository, UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    @Override
    public TicketResponse createTicket(CreateTicketRequest request) {
        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + request.getCreatedById()));
        if (creator.getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only CUSTOMER users can create tickets");
        }

        Ticket ticket = new Ticket();
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setCreatedBy(creator);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse getTicketById(Long id) {
        return TicketResponse.from(findTicket(id));
    }

    @Override
    public TicketResponse updateTicket(Long id, TicketRequest request) {
        Ticket ticket = findTicket(id);
        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setPriority(request.getPriority());
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Override
    public TicketResponse changeStatus(Long id, TicketStatusRequest request) {
        Ticket ticket = findTicket(id);
        TicketStatus currentStatus = ticket.getStatus();
        TicketStatus newStatus = request.getStatus();
        if (!currentStatus.canChangeTo(newStatus)) {
            throw new BadRequestException("Cannot change status from " + currentStatus + " to " + newStatus);
        }

        ticket.setStatus(newStatus);
        return TicketResponse.from(ticketRepository.save(ticket));
    }

    @Override
    public void deleteTicket(Long id) {
        ticketRepository.delete(findTicket(id));
    }

    @Override
    public List<TicketResponse> getTicketsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        List<Ticket> tickets = ticketRepository.findByCreatedById(userId);
        List<TicketResponse> responses = new ArrayList<>();
        for (Ticket ticket : tickets) {
            responses.add(TicketResponse.from(ticket));
        }
        return responses;
    }

    private Ticket findTicket(Long id) {
        return ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id " + id));
    }
}

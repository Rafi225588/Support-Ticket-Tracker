package com.example.supportticket.service;

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketRequest;
import com.example.supportticket.dto.TicketResponse;
import com.example.supportticket.dto.TicketStatusRequest;
import com.example.supportticket.entity.Ticket;
import com.example.supportticket.entity.User;
import com.example.supportticket.enums.Priority;
import com.example.supportticket.enums.Role;
import com.example.supportticket.enums.TicketStatus;
import com.example.supportticket.exception.BadRequestException;
import com.example.supportticket.exception.ResourceNotFoundException;
import com.example.supportticket.repository.TicketRepository;
import com.example.supportticket.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TicketServiceImpl ticketService;

    @Test
    void createTicket_alwaysStartsAsOpen() {
        User customer = user(1L, Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(ticketRepository.save(any(Ticket.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TicketResponse response = ticketService.createTicket(createTicketRequest(1L));

        assertEquals(TicketStatus.OPEN, response.getStatus());
        assertEquals("Rahim Uddin", response.getCreatedBy().getName());
        assertEquals("rahim@example.com", response.getCreatedBy().getEmail());
    }

    @Test
    void createTicket_throwsBadRequest_whenCreatorIsAgent() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(user(2L, Role.AGENT)));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> ticketService.createTicket(createTicketRequest(2L)));

        assertEquals("Only CUSTOMER users can create tickets", exception.getMessage());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void createTicket_throwsNotFound_whenCreatorDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> ticketService.createTicket(createTicketRequest(99L)));

        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @ParameterizedTest
    @CsvSource({
            "OPEN, IN_PROGRESS",
            "OPEN, CLOSED",
            "IN_PROGRESS, RESOLVED",
            "RESOLVED, CLOSED",
            "RESOLVED, IN_PROGRESS"
    })
    void changeStatus_allowsValidTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        Ticket ticket = ticket(10L, currentStatus);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);

        TicketResponse response = ticketService.changeStatus(10L, statusRequest(newStatus));

        assertEquals(newStatus, response.getStatus());
    }

    @ParameterizedTest
    @CsvSource({
            "CLOSED, OPEN",
            "CLOSED, IN_PROGRESS",
            "CLOSED, RESOLVED",
            "CLOSED, CLOSED",
            "OPEN, RESOLVED",
            "OPEN, OPEN",
            "IN_PROGRESS, OPEN",
            "IN_PROGRESS, CLOSED",
            "IN_PROGRESS, IN_PROGRESS",
            "RESOLVED, OPEN",
            "RESOLVED, RESOLVED"
    })
    void changeStatus_blocksInvalidTransition(TicketStatus currentStatus, TicketStatus newStatus) {
        Ticket ticket = ticket(10L, currentStatus);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));

        BadRequestException exception = assertThrows(BadRequestException.class,
                () -> ticketService.changeStatus(10L, statusRequest(newStatus)));

        assertEquals("Cannot change status from " + currentStatus + " to " + newStatus, exception.getMessage());
        assertEquals(currentStatus, ticket.getStatus());
        verify(ticketRepository, never()).save(any(Ticket.class));
    }

    @Test
    void updateTicket_changesFieldsButNotStatus() {
        Ticket ticket = ticket(10L, TicketStatus.IN_PROGRESS);
        when(ticketRepository.findById(10L)).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(ticket)).thenReturn(ticket);
        TicketRequest request = new TicketRequest();
        request.setTitle("Login fails on mobile app");
        request.setDescription("The app shows a blank screen after login");
        request.setPriority(Priority.URGENT);

        TicketResponse response = ticketService.updateTicket(10L, request);

        assertEquals("Login fails on mobile app", response.getTitle());
        assertEquals(Priority.URGENT, response.getPriority());
        assertEquals(TicketStatus.IN_PROGRESS, response.getStatus());
    }

    @Test
    void getTicketById_throwsNotFound_whenTicketDoesNotExist() {
        when(ticketRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> ticketService.getTicketById(99L));

        assertEquals("Ticket not found with id 99", exception.getMessage());
    }

    private CreateTicketRequest createTicketRequest(Long createdById) {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setTitle("Cannot login");
        request.setDescription("Login button does nothing");
        request.setPriority(Priority.HIGH);
        request.setCreatedById(createdById);
        return request;
    }

    private TicketStatusRequest statusRequest(TicketStatus status) {
        TicketStatusRequest request = new TicketStatusRequest();
        request.setStatus(status);
        return request;
    }

    private Ticket ticket(Long id, TicketStatus status) {
        Ticket ticket = new Ticket();
        ticket.setId(id);
        ticket.setTitle("Cannot login");
        ticket.setDescription("Login button does nothing");
        ticket.setPriority(Priority.HIGH);
        ticket.setStatus(status);
        ticket.setCreatedBy(user(1L, Role.CUSTOMER));
        return ticket;
    }

    private User user(Long id, Role role) {
        User user = new User();
        user.setId(id);
        user.setName("Rahim Uddin");
        user.setEmail("rahim@example.com");
        user.setRole(role);
        return user;
    }
}
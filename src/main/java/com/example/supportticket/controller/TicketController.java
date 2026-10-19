package com.example.supportticket.controller;

import com.example.supportticket.dto.CreateTicketRequest;
import com.example.supportticket.dto.TicketRequest;
import com.example.supportticket.dto.TicketResponse;
import com.example.supportticket.dto.TicketStatusRequest;
import com.example.supportticket.iservice.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ticketService.createTicket(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(@PathVariable Long id,
                                                       @Valid @RequestBody TicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> changeStatus(@PathVariable Long id,
                                                       @Valid @RequestBody TicketStatusRequest request) {
        return ResponseEntity.ok(ticketService.changeStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(@PathVariable Long id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }
}

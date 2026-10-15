package com.example.supportticket.repository;

import com.example.supportticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    boolean existsByCreatedById(Long userId);

    List<Ticket> findByCreatedById(Long userId);
}

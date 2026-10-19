package com.example.supportticket.dto;

import com.example.supportticket.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class TicketStatusRequest {

    @NotNull(message = "Status is required")
    private TicketStatus status;

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}

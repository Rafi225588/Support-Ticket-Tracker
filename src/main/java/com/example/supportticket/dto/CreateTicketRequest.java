package com.example.supportticket.dto;

import jakarta.validation.constraints.NotNull;

public class CreateTicketRequest extends TicketRequest {

    @NotNull(message = "createdById is required")
    private Long createdById;

    public Long getCreatedById() {
        return createdById;
    }

    public void setCreatedById(Long createdById) {
        this.createdById = createdById;
    }
}

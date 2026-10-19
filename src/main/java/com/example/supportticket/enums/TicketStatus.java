package com.example.supportticket.enums;

import java.util.List;

public enum TicketStatus {
    OPEN,
    IN_PROGRESS,
    RESOLVED,
    CLOSED;

    public List<TicketStatus> allowedNextStatuses() {
        return switch (this) {
            case OPEN -> List.of(IN_PROGRESS, CLOSED);
            case IN_PROGRESS -> List.of(RESOLVED);
            case RESOLVED -> List.of(CLOSED, IN_PROGRESS);
            case CLOSED -> List.of();
        };
    }

    public boolean canChangeTo(TicketStatus newStatus) {
        return allowedNextStatuses().contains(newStatus);
    }
}
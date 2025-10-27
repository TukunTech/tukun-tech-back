package com.upc.tukuntech.backend.modules.support.application.commands;

import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;

public record UpdateTicketStatusCommand(
        Long ticketId,
        TicketStatus status
) {}

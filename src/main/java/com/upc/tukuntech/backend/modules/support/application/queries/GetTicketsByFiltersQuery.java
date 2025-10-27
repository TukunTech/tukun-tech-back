package com.upc.tukuntech.backend.modules.support.application.queries;

import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;

import java.time.LocalDate;

public record GetTicketsByFiltersQuery(
        TicketStatus status,
        Long userId,
        LocalDate fromDate,
        LocalDate toDate
) {}

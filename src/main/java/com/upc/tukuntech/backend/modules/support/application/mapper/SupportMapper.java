package com.upc.tukuntech.backend.modules.support.application.mapper;

import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.domain.entity.SupportResponse;
import com.upc.tukuntech.backend.modules.support.domain.entity.SupportTicket;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class SupportMapper {

    public TicketResponse toResponse(SupportTicket ticket) {
        if (ticket == null)
            return null;

        return new TicketResponse(
                ticket.getId(),
                ticket.getUserId(),
                ticket.getName(),
                ticket.getEmail(),
                ticket.getSubject(),
                ticket.getDescription(),
                ticket.getStatus(),
                ticket.getCreatedAt(),
                ticket.getResponses() != null
                        ? ticket.getResponses()
                        .stream()
                        .map(this::toSummary)
                        .collect(Collectors.toList())
                        : Collections.emptyList()
        );
    }

    private TicketResponse.ResponseSummary toSummary(SupportResponse response) {
        if (response == null)
            return null;

        return new TicketResponse.ResponseSummary(
                response.getResponderId(),
                response.getMessage(),
                response.getRespondedAt()
        );
    }
}

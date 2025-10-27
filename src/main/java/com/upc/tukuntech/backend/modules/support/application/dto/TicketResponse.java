package com.upc.tukuntech.backend.modules.support.application.dto;

import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO principal para representar un ticket de soporte con sus respuestas asociadas.
 */
public record TicketResponse(
        Long id,
        Long userId,
        String name,
        String email,
        String subject,
        String description,
        TicketStatus status,
        LocalDateTime createdAt,
        List<ResponseSummary> responses
) {

    /**
     * Subrecord que representa un resumen de respuesta asociado al ticket.
     */
    public record ResponseSummary(
            Long responderId,
            String message,
            LocalDateTime respondedAt
    ) {}
}

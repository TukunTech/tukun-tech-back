package com.upc.tukuntech.backend.modules.support.application.queries;

/**
 * Query para obtener los tickets creados por un usuario específico.
 * Utilizado por el handler GetTicketsByUserQueryHandler.
 */
public record GetTicketsByUserQuery(Long userId) {}


package com.upc.tukuntech.backend.modules.support.application.service;

import com.upc.tukuntech.backend.modules.support.application.commands.CreateResponseCommand;
import com.upc.tukuntech.backend.modules.support.application.commands.CreateTicketCommand;
import com.upc.tukuntech.backend.modules.support.application.commands.UpdateTicketStatusCommand;
import com.upc.tukuntech.backend.modules.support.application.commands.handlers.CreateResponseCommandHandler;
import com.upc.tukuntech.backend.modules.support.application.commands.handlers.CreateTicketCommandHandler;
import com.upc.tukuntech.backend.modules.support.application.commands.handlers.UpdateTicketStatusCommandHandler;
import com.upc.tukuntech.backend.modules.support.application.dto.CreateResponseRequest;
import com.upc.tukuntech.backend.modules.support.application.dto.CreateTicketRequest;
import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.queries.GetTicketsByFiltersQuery;
import com.upc.tukuntech.backend.modules.support.application.queries.GetTicketsByUserQuery;
import com.upc.tukuntech.backend.modules.support.application.queries.handlers.GetTicketsByFiltersQueryHandler;
import com.upc.tukuntech.backend.modules.support.application.queries.handlers.GetTicketsByUserQueryHandler;
import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class SupportApplicationService {

    private final CreateTicketCommandHandler createHandler;
    private final CreateResponseCommandHandler createResponseHandler;
    private final UpdateTicketStatusCommandHandler updateTicketStatusHandler;
    private final GetTicketsByFiltersQueryHandler getTicketsByFiltersHandler;
    private final GetTicketsByUserQueryHandler getTicketsByUserHandler;

    public SupportApplicationService(CreateTicketCommandHandler createHandler,
                                     CreateResponseCommandHandler createResponseHandler,
                                     UpdateTicketStatusCommandHandler updateTicketStatusHandler,
                                     GetTicketsByFiltersQueryHandler getTicketsByFiltersHandler,
                                     GetTicketsByUserQueryHandler getTicketsByUserHandler) {

        this.createHandler = createHandler;
        this.createResponseHandler = createResponseHandler;
        this.updateTicketStatusHandler = updateTicketStatusHandler;
        this.getTicketsByFiltersHandler = getTicketsByFiltersHandler;
        this.getTicketsByUserHandler = getTicketsByUserHandler;
    }

    // ✅ Crear ticket
    public TicketResponse createTicket(Long userId, CreateTicketRequest request) {
        var cmd = new CreateTicketCommand(
                userId,
                request.name(),
                request.email(),
                request.subject(),
                request.description()
        );
        return createHandler.handle(cmd);
    }

    // ✅ Agregar respuesta (admin)
    public TicketResponse addResponse(Long ticketId, Long responderId, CreateResponseRequest request) {
        var cmd = new CreateResponseCommand(ticketId, responderId, request.message());
        return createResponseHandler.handle(cmd);
    }

    // ✅ Actualizar estado
    public TicketResponse updateTicketStatus(Long ticketId, String status) {
        TicketStatus parsedStatus;
        try {
            parsedStatus = TicketStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
        }

        var cmd = new UpdateTicketStatusCommand(ticketId, parsedStatus);
        return updateTicketStatusHandler.handle(cmd);
    }

    // ✅ Filtrar tickets (admin)
    public List<TicketResponse> getTicketsByFilters(
            String status, Long userId, LocalDate fromDate, LocalDate toDate
    ) {
        var parsedStatus = status != null ? TicketStatus.valueOf(status.toUpperCase()) : null;
        var query = new GetTicketsByFiltersQuery(parsedStatus, userId, fromDate, toDate);
        return getTicketsByFiltersHandler.handle(query);
    }

    // ✅ Nuevo: obtener tickets del usuario autenticado
    public List<TicketResponse> getTicketsByUser(Long userId) {
        var query = new GetTicketsByUserQuery(userId);
        return getTicketsByUserHandler.handle(query);
    }
}

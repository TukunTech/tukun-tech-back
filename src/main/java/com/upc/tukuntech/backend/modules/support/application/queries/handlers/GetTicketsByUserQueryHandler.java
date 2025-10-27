package com.upc.tukuntech.backend.modules.support.application.queries.handlers;

import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.mapper.SupportMapper;
import com.upc.tukuntech.backend.modules.support.application.queries.GetTicketsByUserQuery;
import com.upc.tukuntech.backend.modules.support.domain.repositories.SupportTicketRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class GetTicketsByUserQueryHandler {

    private final SupportTicketRepository repository;
    private final SupportMapper mapper;

    public GetTicketsByUserQueryHandler(SupportTicketRepository repository, SupportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * 🔹 Retorna todos los tickets creados por el usuario con el ID especificado.
     * Lanza 404 si el usuario no tiene tickets registrados.
     */
    public List<TicketResponse> handle(GetTicketsByUserQuery query) {
        var tickets = repository.findByUserId(query.userId());

        if (tickets.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No tickets found for user with ID: " + query.userId());
        }

        return tickets.stream()
                .map(mapper::toResponse)
                .toList();
    }
}

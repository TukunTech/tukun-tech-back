package com.upc.tukuntech.backend.modules.support.application.commands.handlers;

import com.upc.tukuntech.backend.modules.support.application.commands.UpdateTicketStatusCommand;
import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.mapper.SupportMapper;
import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;
import com.upc.tukuntech.backend.modules.support.domain.repositories.SupportTicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UpdateTicketStatusCommandHandler {

    private final SupportTicketRepository repository;
    private final SupportMapper mapper;

    public UpdateTicketStatusCommandHandler(SupportTicketRepository repository, SupportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public TicketResponse handle(UpdateTicketStatusCommand cmd) {
        var ticket = repository.findById(cmd.ticketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ticket already closed");

        ticket.setStatus(cmd.status());
        var saved = repository.save(ticket);

        return mapper.toResponse(saved);
    }
}

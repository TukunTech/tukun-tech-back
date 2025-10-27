package com.upc.tukuntech.backend.modules.support.application.commands.handlers;

import com.upc.tukuntech.backend.modules.support.application.commands.CreateTicketCommand;
import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.mapper.SupportMapper;
import com.upc.tukuntech.backend.modules.support.domain.entity.SupportTicket;
import com.upc.tukuntech.backend.modules.support.domain.repositories.SupportTicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CreateTicketCommandHandler {

    private final SupportTicketRepository repository;
    private final SupportMapper mapper;

    public CreateTicketCommandHandler(SupportTicketRepository repository, SupportMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    public TicketResponse handle(CreateTicketCommand cmd) {
        if (cmd.name() == null || cmd.name().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be empty");
        if (cmd.email() == null || cmd.email().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be empty");
        if (cmd.subject() == null || cmd.subject().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject cannot be empty");
        if (cmd.description() == null || cmd.description().isBlank())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Description cannot be empty");

        SupportTicket ticket = new SupportTicket();
        ticket.setUserId(cmd.userId());
        ticket.setName(cmd.name());
        ticket.setEmail(cmd.email());
        ticket.setSubject(cmd.subject());
        ticket.setDescription(cmd.description());

        var saved = repository.save(ticket);
        return mapper.toResponse(saved);
    }
}
package com.upc.tukuntech.backend.modules.support.application.commands.handlers;

import com.upc.tukuntech.backend.modules.support.application.commands.CreateResponseCommand;
import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.mapper.SupportMapper;
import com.upc.tukuntech.backend.modules.support.domain.entity.SupportResponse;
import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;
import com.upc.tukuntech.backend.modules.support.domain.repositories.SupportResponseRepository;
import com.upc.tukuntech.backend.modules.support.domain.repositories.SupportTicketRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CreateResponseCommandHandler {

    private final SupportTicketRepository ticketRepository;
    private final SupportResponseRepository responseRepository;
    private final SupportMapper mapper;

    public CreateResponseCommandHandler(
            SupportTicketRepository ticketRepository,
            SupportResponseRepository responseRepository,
            SupportMapper mapper
    ) {
        this.ticketRepository = ticketRepository;
        this.responseRepository = responseRepository;
        this.mapper = mapper;
    }

    @Transactional
    public TicketResponse handle(CreateResponseCommand cmd) {
        var ticket = ticketRepository.findById(cmd.ticketId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));

        if (ticket.getStatus() == TicketStatus.CLOSED)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot respond to a closed ticket");

        SupportResponse response = new SupportResponse();
        response.setResponderId(cmd.responderId());
        response.setMessage(cmd.message());
        response.setTicket(ticket);

        responseRepository.save(response);
        ticket.getResponses().add(response);
        ticket.setStatus(TicketStatus.IN_PROGRESS);

        var savedTicket = ticketRepository.save(ticket);
        return mapper.toResponse(savedTicket);
    }
}

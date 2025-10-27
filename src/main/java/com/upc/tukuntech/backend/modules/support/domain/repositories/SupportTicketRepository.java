package com.upc.tukuntech.backend.modules.support.domain.repositories;

import com.upc.tukuntech.backend.modules.support.domain.entity.SupportTicket;
import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupportTicketRepository
        extends JpaRepository<SupportTicket, Long>,
        JpaSpecificationExecutor<SupportTicket> {

    // ✅ Carga automáticamente las respuestas asociadas (resuelve el "no Session")
    @EntityGraph(attributePaths = {"responses"})
    List<SupportTicket> findByUserId(Long userId);

    @EntityGraph(attributePaths = {"responses"})
    List<SupportTicket> findByStatus(TicketStatus status);

    // ✅ Igual para búsquedas con Specification (GET /support/tickets)
    @Override
    @EntityGraph(attributePaths = {"responses"})
    List<SupportTicket> findAll(org.springframework.data.jpa.domain.Specification<SupportTicket> spec);
}

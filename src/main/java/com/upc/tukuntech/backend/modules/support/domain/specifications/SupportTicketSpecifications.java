package com.upc.tukuntech.backend.modules.support.domain.specifications;

import com.upc.tukuntech.backend.modules.support.domain.entity.SupportTicket;
import com.upc.tukuntech.backend.modules.support.domain.model.valueobjects.TicketStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class SupportTicketSpecifications {

    private SupportTicketSpecifications() {

    }

    @SuppressWarnings("NullableProblems")
    public static Specification<SupportTicket> hasStatus(TicketStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    @SuppressWarnings("NullableProblems")
    public static Specification<SupportTicket> hasUserId(Long userId) {
        return (root, query, cb) -> cb.equal(root.get("userId"), userId);
    }

    @SuppressWarnings("NullableProblems")
    public static Specification<SupportTicket> createdAfter(LocalDateTime from) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    @SuppressWarnings("NullableProblems")
    public static Specification<SupportTicket> createdBefore(LocalDateTime to) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to);
    }

    /**
     * ✅ Construye dinámicamente un Specification combinando los filtros opcionales.
     * Usa Specification.allOf() (Spring 3.5+) en lugar de where().
     */
    public static Specification<SupportTicket> buildDynamicFilter(
            TicketStatus status, Long userId, LocalDate fromDate, LocalDate toDate) {

        Specification<SupportTicket> spec = Specification.allOf();

        if (status != null)
            spec = spec.and(hasStatus(status));

        if (userId != null)
            spec = spec.and(hasUserId(userId));

        if (fromDate != null)
            spec = spec.and(createdAfter(fromDate.atStartOfDay()));

        if (toDate != null)
            spec = spec.and(createdBefore(toDate.atTime(23, 59, 59)));

        return spec;
    }
}

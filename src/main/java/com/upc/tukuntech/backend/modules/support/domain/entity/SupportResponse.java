package com.upc.tukuntech.backend.modules.support.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_responses")
@Getter
@Setter
public class SupportResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // quién respondió (admin o usuario)
    private Long responderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private LocalDateTime respondedAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private SupportTicket ticket;
}

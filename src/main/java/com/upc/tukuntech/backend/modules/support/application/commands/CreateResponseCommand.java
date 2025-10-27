package com.upc.tukuntech.backend.modules.support.application.commands;

public record CreateResponseCommand(
        Long ticketId,
        Long responderId,
        String message
) {}

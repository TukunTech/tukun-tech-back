package com.upc.tukuntech.backend.modules.auth.application.dto;

public record RegisterResponse(
        Long id,
        String email,
        String message
) {}

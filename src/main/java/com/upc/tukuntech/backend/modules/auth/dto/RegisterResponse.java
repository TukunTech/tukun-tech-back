package com.upc.tukuntech.backend.modules.auth.dto;

public record RegisterResponse(
        Long id,
        String email,
        String message
) {}

package com.upc.tukuntech.backend.modules.iam.application.dto;

public record RegisterResponse(
        Long id,
        String email,
        String message
) {}

package com.upc.tukuntech.backend.modules.auth.application.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken,
        UserSummary user
) {}

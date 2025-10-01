package com.upc.tukuntech.backend.modules.auth.application.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken
) {}
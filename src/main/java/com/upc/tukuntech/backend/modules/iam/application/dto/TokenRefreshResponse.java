package com.upc.tukuntech.backend.modules.iam.application.dto;

public record TokenRefreshResponse(
        String accessToken,
        String tokenType,
        long expiresInSeconds,
        String refreshToken
) {}
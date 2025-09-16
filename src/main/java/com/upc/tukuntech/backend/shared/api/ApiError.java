package com.upc.tukuntech.backend.shared.api;

import java.time.Instant;
import java.util.List;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<String> details
) {
    public static ApiError of(int status, String error, String message, String path, List<String> details) {
        return new ApiError(Instant.now(), status, error, message, path, details);
    }
    public static ApiError of(int status, String error, String message, String path) {
        return of(status, error, message, path, List.of());
    }
}

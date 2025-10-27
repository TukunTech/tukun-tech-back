package com.upc.tukuntech.backend.modules.monitoring.application.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida para representar alertas generadas por signos vitales anómalos.
 * Se usa en REST y también para eventos SSE (alert).
 */
public record AlertResponse(
        Long id,
        Long patientId,
        Long deviceId,
        String type,      // TEMPERATURE / HEART_RATE / OXYGENATION
        String severity,  // CRITICAL / HIGH / LOW
        String message,   // Descripción human-readable
        LocalDateTime createdAt
) {}

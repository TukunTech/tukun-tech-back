package com.upc.tukuntech.backend.modules.monitoring.application.dto;

import java.time.LocalDateTime;

/**
 * DTO de salida que representa una medición validada o registrada.
 * Se usa tanto en REST como en eventos SSE en tiempo real.
 */
public record VitalSignResponse(
        Long id,
        Long patientId,
        Long deviceId,
        Integer heartRate,
        Integer oxygenLevel,
        Double temperature,
        LocalDateTime timestamp
) {}

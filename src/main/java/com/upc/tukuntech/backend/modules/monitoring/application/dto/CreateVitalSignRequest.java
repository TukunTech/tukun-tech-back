package com.upc.tukuntech.backend.modules.monitoring.application.dto;

import jakarta.validation.constraints.*;

/**
 * DTO de entrada para registrar una nueva medición de signos vitales.
 * Se valida automáticamente con Bean Validation (Jakarta).
 */
public record CreateVitalSignRequest(

        @NotNull(message = "El ID del paciente es obligatorio")
        Long patientId,

        @NotNull(message = "El ID del dispositivo es obligatorio")
        Long deviceId,

        @NotNull(message = "El ritmo cardíaco no puede ser nulo")
        @Min(value = 30, message = "El ritmo cardíaco debe ser al menos 30 bpm")
        @Max(value = 220, message = "El ritmo cardíaco no puede superar 220 bpm")
        Integer heartRate, // bpm

        @NotNull(message = "El nivel de oxígeno no puede ser nulo")
        @Min(value = 70, message = "El nivel de oxígeno debe ser al menos 70%")
        @Max(value = 100, message = "El nivel de oxígeno no puede superar 100%")
        Integer oxygenLevel, // SpO2 %

        @NotNull(message = "La temperatura no puede ser nula")
        @DecimalMin(value = "30.0", message = "La temperatura mínima aceptada es 30.0°C")
        @DecimalMax(value = "43.0", message = "La temperatura máxima aceptada es 43.0°C")
        Double temperature // °C
) {}

package com.upc.tukuntech.backend.modules.monitoring.application.commands;

public record CreateVitalSignCommand(
        Long patientId,
        Long deviceId,
        Integer heartRate,
        Integer oxygenLevel,
        Double temperature
) {}
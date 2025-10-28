package com.upc.tukuntech.backend.modules.caremanagement.application.commands;

public record UnassignPatientCommand(
        Long caregiverId,
        Long patientId
) {}

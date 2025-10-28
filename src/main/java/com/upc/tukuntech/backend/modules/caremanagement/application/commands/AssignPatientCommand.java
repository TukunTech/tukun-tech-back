package com.upc.tukuntech.backend.modules.caremanagement.application.commands;

public record AssignPatientCommand(
        Long caregiverId,
        Long patientId
) {}

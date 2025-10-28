package com.upc.tukuntech.backend.modules.reports.application.commands;

import java.time.LocalDate;

public record GenerateDailyReportCommand(Long patientId, LocalDate date) {}

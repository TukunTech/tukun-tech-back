package com.upc.tukuntech.backend.modules.reports.application.queries;

import java.time.LocalDate;

public record GetAlertSummaryQuery(Long patientId, LocalDate startDate, LocalDate endDate) {}

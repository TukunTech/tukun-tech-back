package com.upc.tukuntech.backend.modules.reports.application.queries;

import java.time.LocalDate;

public record GetDailyReportQuery(Long patientId, LocalDate date) {}

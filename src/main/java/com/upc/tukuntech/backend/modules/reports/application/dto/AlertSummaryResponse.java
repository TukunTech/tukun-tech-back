package com.upc.tukuntech.backend.modules.reports.application.dto;

import java.time.LocalDate;
import java.util.Map;

public record AlertSummaryResponse(
        Long patientId,
        LocalDate startDate,
        LocalDate endDate,
        Map<String, Long> alertsByType,
        Map<String, Long> alertsBySeverity,
        Integer totalAlerts
) {}

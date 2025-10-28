package com.upc.tukuntech.backend.modules.reports.application.dto;

import java.time.LocalDate;

public record DailyReportResponse(
        Long patientId,
        LocalDate date,
        Double avgHeartRate,
        Double avgOxygenLevel,
        Double avgTemperature,
        Integer totalAlerts
) {}

package com.upc.tukuntech.backend.modules.reports.application.mapper;

import com.upc.tukuntech.backend.modules.reports.application.dto.DailyReportResponse;
import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;
import org.springframework.stereotype.Component;

@Component
public class ReportMapper {

    public DailyReportResponse toResponse(DailyReport entity) {
        if (entity == null) return null;
        return new DailyReportResponse(
                entity.getPatientId(),
                entity.getDate(),
                entity.getAvgHeartRate(),
                entity.getAvgOxygenLevel(),
                entity.getAvgTemperature(),
                entity.getTotalAlerts()
        );
    }
}

package com.upc.tukuntech.backend.modules.reports.infrastructure.mapper;

import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;
import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReportEntity;
import org.springframework.stereotype.Component;

@Component
public class ReportEntityMapper {

    public DailyReportEntity toEntity(DailyReport domain) {
        var e = new DailyReportEntity();
        e.setId(domain.getId());
        e.setPatientId(domain.getPatientId());
        e.setDate(domain.getDate());
        e.setAvgHeartRate(domain.getAvgHeartRate());
        e.setAvgOxygenLevel(domain.getAvgOxygenLevel());
        e.setAvgTemperature(domain.getAvgTemperature());
        e.setTotalAlerts(domain.getTotalAlerts());
        return e;
    }

    public DailyReport toDomain(DailyReportEntity e) {
        var d = DailyReport.create(
                e.getPatientId(),
                e.getDate(),
                e.getAvgHeartRate(),
                e.getAvgOxygenLevel(),
                e.getAvgTemperature(),
                e.getTotalAlerts()
        );
        d.setId(e.getId());
        return d;
    }
}

package com.upc.tukuntech.backend.modules.reports.application.queries.handlers;

import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.AlertRepository;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import com.upc.tukuntech.backend.modules.reports.application.dto.DailyReportResponse;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetDailyReportQuery;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class GetDailyReportQueryHandler {

    private final VitalSignRecordRepository vitalRepo;
    private final AlertRepository alertRepo;

    public GetDailyReportQueryHandler(VitalSignRecordRepository vitalRepo, AlertRepository alertRepo) {
        this.vitalRepo = vitalRepo;
        this.alertRepo = alertRepo;
    }

    public DailyReportResponse handle(GetDailyReportQuery query) {
        var date = query.date();
        var start = date.atStartOfDay();
        var end = date.atTime(LocalTime.MAX);

        var records = vitalRepo.findByTimestampBetween(start, end)
                .stream()
                .filter(r -> r.getPatientId().equals(query.patientId()))
                .toList();

        if (records.isEmpty()) {
            return new DailyReportResponse(query.patientId(), date, null, null, null, 0);
        }

        double avgHR = records.stream().mapToInt(r -> r.getHeartRate().getValue()).average().orElse(0);
        double avgSpO2 = records.stream().mapToInt(r -> r.getOxygenLevel().getValue()).average().orElse(0);
        double avgTemp = records.stream().mapToDouble(r -> r.getTemperature().getValue()).average().orElse(0);

        int alertCount = (int) alertRepo.findByPatientId(query.patientId()).stream()
                .filter(a -> a.getCreatedAt().toLocalDate().equals(date))
                .count();

        return new DailyReportResponse(query.patientId(), date, avgHR, avgSpO2, avgTemp, alertCount);
    }
}

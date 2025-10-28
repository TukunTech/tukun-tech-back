package com.upc.tukuntech.backend.modules.reports.application.commands.handlers;

import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.AlertRepository;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;
import com.upc.tukuntech.backend.modules.reports.domain.repositories.DailyReportRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class GenerateDailyReportCommandHandler {

    private final VitalSignRecordRepository vitalRepo;
    private final AlertRepository alertRepo;
    private final DailyReportRepository reportRepo;

    public GenerateDailyReportCommandHandler(
            VitalSignRecordRepository vitalRepo,
            AlertRepository alertRepo,
            DailyReportRepository reportRepo
    ) {
        this.vitalRepo = vitalRepo;
        this.alertRepo = alertRepo;
        this.reportRepo = reportRepo;
    }

    public DailyReport handle(Long patientId, LocalDate date) {
        var start = date.atStartOfDay();
        var end = date.atTime(LocalTime.MAX);

        var records = vitalRepo.findByTimestampBetween(start, end)
                .stream()
                .filter(r -> r.getPatientId().equals(patientId))
                .toList();

        if (records.isEmpty()) {
            throw new IllegalStateException("No measurements found for patient " + patientId + " on " + date);
        }

        double avgHR = records.stream().mapToInt(r -> r.getHeartRate().getValue()).average().orElse(0);
        double avgSpO2 = records.stream().mapToInt(r -> r.getOxygenLevel().getValue()).average().orElse(0);
        double avgTemp = records.stream().mapToDouble(r -> r.getTemperature().getValue()).average().orElse(0);

        int alertCount = (int) alertRepo.findByPatientId(patientId).stream()
                .filter(a -> a.getCreatedAt().toLocalDate().equals(date))
                .count();

        // crea y guarda el reporte diario
        var report = DailyReport.create(patientId, date, avgHR, avgSpO2, avgTemp, alertCount);
        return reportRepo.save(report);
    }
}

package com.upc.tukuntech.backend.modules.reports.application.queries.handlers;

import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.AlertRepository;
import com.upc.tukuntech.backend.modules.reports.application.dto.AlertSummaryResponse;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetAlertSummaryQuery;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class GetAlertSummaryQueryHandler {

    private final AlertRepository alertRepo;

    public GetAlertSummaryQueryHandler(AlertRepository alertRepo) {
        this.alertRepo = alertRepo;
    }

    public AlertSummaryResponse handle(GetAlertSummaryQuery query) {
        var alerts = alertRepo.findByPatientId(query.patientId()).stream()
                .filter(a -> {
                    var date = a.getCreatedAt().toLocalDate();
                    return !date.isBefore(query.startDate()) && !date.isAfter(query.endDate());
                })
                .toList();

        var byType = alerts.stream()
                .collect(Collectors.groupingBy(a -> a.getType(), Collectors.counting()));

        var bySeverity = alerts.stream()
                .collect(Collectors.groupingBy(a -> a.getSeverity(), Collectors.counting()));

        return new AlertSummaryResponse(query.patientId(), query.startDate(), query.endDate(), byType, bySeverity, alerts.size());
    }
}

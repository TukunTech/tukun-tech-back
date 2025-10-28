package com.upc.tukuntech.backend.modules.reports.application.service;

import com.upc.tukuntech.backend.modules.reports.application.commands.GenerateDailyReportCommand;
import com.upc.tukuntech.backend.modules.reports.application.commands.handlers.GenerateDailyReportCommandHandler;
import com.upc.tukuntech.backend.modules.reports.application.dto.AlertSummaryResponse;
import com.upc.tukuntech.backend.modules.reports.application.dto.DailyReportResponse;
import com.upc.tukuntech.backend.modules.reports.application.mapper.ReportMapper;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetAlertSummaryQuery;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetDailyReportQuery;
import com.upc.tukuntech.backend.modules.reports.application.queries.handlers.GetAlertSummaryQueryHandler;
import com.upc.tukuntech.backend.modules.reports.application.queries.handlers.GetDailyReportQueryHandler;
import com.upc.tukuntech.backend.modules.reports.domain.entity.DailyReport;
import com.upc.tukuntech.backend.modules.reports.domain.repositories.DailyReportRepository;
import org.springframework.stereotype.Service;

@Service
public class ReportsApplicationService {

    private final GetDailyReportQueryHandler dailyHandler;
    private final GetAlertSummaryQueryHandler alertHandler;
    private final GenerateDailyReportCommandHandler generateHandler;
    private final DailyReportRepository reportRepo;
    private final ReportMapper mapper;

    public ReportsApplicationService(
            GetDailyReportQueryHandler dailyHandler,
            GetAlertSummaryQueryHandler alertHandler,
            GenerateDailyReportCommandHandler generateHandler,
            DailyReportRepository reportRepo,
            ReportMapper mapper
    ) {
        this.dailyHandler = dailyHandler;
        this.alertHandler = alertHandler;
        this.generateHandler = generateHandler;
        this.reportRepo = reportRepo;
        this.mapper = mapper;
    }

    // 🔹 Query Side
    public DailyReportResponse getDailyReport(GetDailyReportQuery query) {
        var response = dailyHandler.handle(query);
        // opcionalmente, guarda el snapshot si no existe
        reportRepo.findByPatientIdAndDate(response.patientId(), response.date())
                .orElseGet(() -> reportRepo.save(DailyReport.create(
                        response.patientId(),
                        response.date(),
                        response.avgHeartRate(),
                        response.avgOxygenLevel(),
                        response.avgTemperature(),
                        response.totalAlerts()
                )));
        return response;
    }

    public AlertSummaryResponse getAlertSummary(GetAlertSummaryQuery query) {
        return alertHandler.handle(query);
    }

    // 🔸 Command Side
    public DailyReportResponse generateDailyReport(GenerateDailyReportCommand command) {
        var report = generateHandler.handle(command.patientId(), command.date());
        return mapper.toResponse(report);
    }
}

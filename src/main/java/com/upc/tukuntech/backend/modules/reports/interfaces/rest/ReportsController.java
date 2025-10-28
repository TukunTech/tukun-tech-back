package com.upc.tukuntech.backend.modules.reports.interfaces.rest;

import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.reports.application.commands.GenerateDailyReportCommand;
import com.upc.tukuntech.backend.modules.reports.application.dto.AlertSummaryResponse;
import com.upc.tukuntech.backend.modules.reports.application.dto.DailyReportResponse;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetAlertSummaryQuery;
import com.upc.tukuntech.backend.modules.reports.application.queries.GetDailyReportQuery;
import com.upc.tukuntech.backend.modules.reports.application.service.ReportsApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Endpoints for daily monitoring and alert reports")
@SecurityRequirement(name = "bearerAuth")
public class ReportsController {

    private final ReportsApplicationService reportsApp;
    private final AuthApplicationService authApp;

    public ReportsController(
            ReportsApplicationService reportsApp,
            AuthApplicationService authApp
    ) {
        this.reportsApp = reportsApp;
        this.authApp = authApp;
    }

    // ------------------------------------------------------------------------------------------
    // 👤 Paciente o Cuidador autenticado: obtiene su propio reporte diario
    // ------------------------------------------------------------------------------------------
    @GetMapping("/me/daily")
    @PreAuthorize("hasAnyRole('PATIENT','ATTENDANT')")
    @Operation(
            summary = "Get daily report for the authenticated user (patient or attendant)",
            description = """
            Returns the average vital signs recorded for the authenticated user on the selected date.
            If no date is provided, today's date will be used.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Daily report retrieved successfully",
                            content = @Content(
                                    schema = @Schema(implementation = DailyReportResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Normal day report",
                                                    value = """
                                                    {
                                                      "userId": 45,
                                                      "date": "2025-10-27",
                                                      "heartRateAverage": 78.5,
                                                      "oxygenLevelAverage": 97.2,
                                                      "temperatureAverage": 36.6,
                                                      "totalMeasurements": 12,
                                                      "alertCount": 0
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Critical day report",
                                                    value = """
                                                    {
                                                      "userId": 45,
                                                      "date": "2025-10-27",
                                                      "heartRateAverage": 128.4,
                                                      "oxygenLevelAverage": 83.1,
                                                      "temperatureAverage": 39.0,
                                                      "totalMeasurements": 10,
                                                      "alertCount": 3
                                                    }
                                                    """
                                            )
                                    }
                            ))
            }
    )
    public ResponseEntity<DailyReportResponse> getMyDailyReport(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var identity = authApp.getAuthenticatedIdentity();
        var query = new GetDailyReportQuery(identity.id(), date != null ? date : LocalDate.now());
        return ResponseEntity.ok(reportsApp.getDailyReport(query));
    }

    // ------------------------------------------------------------------------------------------
    // ⚠️ Alert summary (para usuarios autenticados)
    // ------------------------------------------------------------------------------------------
    @GetMapping("/me/alerts")
    @PreAuthorize("hasAnyRole('PATIENT','ATTENDANT','ADMINISTRATOR')")
    @Operation(
            summary = "Get alert summary for the authenticated user within a date range",
            description = """
            Returns a summary of alerts generated for the authenticated user between the provided start and end dates.
            Useful for analyzing weekly or monthly patterns.
            """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Alert summary retrieved successfully",
                            content = @Content(
                                    schema = @Schema(implementation = AlertSummaryResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Weekly summary (no alerts)",
                                                    value = """
                                                    {
                                                      "userId": 45,
                                                      "startDate": "2025-10-20",
                                                      "endDate": "2025-10-27",
                                                      "totalAlerts": 0,
                                                      "criticalAlerts": 0,
                                                      "mildAlerts": 0
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Weekly summary (with alerts)",
                                                    value = """
                                                    {
                                                      "userId": 45,
                                                      "startDate": "2025-10-20",
                                                      "endDate": "2025-10-27",
                                                      "totalAlerts": 4,
                                                      "criticalAlerts": 2,
                                                      "mildAlerts": 2
                                                    }
                                                    """
                                            )
                                    }
                            ))
            }
    )
    public ResponseEntity<AlertSummaryResponse> getMyAlertSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        var identity = authApp.getAuthenticatedIdentity();
        var query = new GetAlertSummaryQuery(identity.id(), startDate, endDate);
        return ResponseEntity.ok(reportsApp.getAlertSummary(query));
    }

    // ------------------------------------------------------------------------------------------
    // 🧮 Administrador: generar reporte manualmente
    // ------------------------------------------------------------------------------------------
    @PostMapping("/generate/{userId}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Generate and persist daily report for a user (admin only)",
            description = "Allows an administrator to manually trigger report generation for any user."
    )
    public ResponseEntity<DailyReportResponse> generateDailyReport(
            @PathVariable Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        var command = new GenerateDailyReportCommand(userId, date);
        return ResponseEntity.ok(reportsApp.generateDailyReport(command));
    }
}

package com.upc.tukuntech.backend.modules.monitoring.interfaces.rest;

import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.monitoring.application.dto.AlertResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.dto.CreateVitalSignRequest;
import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.service.AlertApplicationService;
import com.upc.tukuntech.backend.modules.monitoring.application.service.MonitoringApplicationService;
import com.upc.tukuntech.backend.modules.monitoring.infrastructure.realtime.SseEmitterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.List;

@Tag(name = "Monitoring", description = "Endpoints for IoT vital sign monitoring")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/monitoring")
public class MonitoringController {

    private final MonitoringApplicationService monitoringApp;
    private final AlertApplicationService alertApp;
    private final SseEmitterService emitterService;
    private final AuthApplicationService authApp;

    public MonitoringController(
            MonitoringApplicationService monitoringApp,
            SseEmitterService emitterService,
            AlertApplicationService alertApp,
            AuthApplicationService authApp
    ) {
        this.monitoringApp = monitoringApp;
        this.emitterService = emitterService;
        this.alertApp = alertApp;
        this.authApp = authApp;
    }

    // ---- Measurements ----
    @Operation(
            summary = "Submit new vital sign measurement",
            description = """
            Receives a new set of vital signs (heart rate, oxygen level, temperature)
            from an authenticated patient's IoT device or mobile app.
            The patientId is automatically resolved from the authenticated identity.
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Payload with vital sign data.",
                    content = @Content(
                            schema = @Schema(implementation = CreateVitalSignRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "Normal measurement",
                                            value = """
                                            {
                                              "deviceId": 101,
                                              "heartRate": 78,
                                              "oxygenLevel": 97,
                                              "temperature": 36.6
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "Critical measurement",
                                            value = """
                                            {
                                              "deviceId": 101,
                                              "heartRate": 140,
                                              "oxygenLevel": 85,
                                              "temperature": 39.2
                                            }
                                            """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Measurement created successfully",
                            content = @Content(schema = @Schema(implementation = VitalSignResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content)
            }
    )
    @PostMapping("/measurements")
    @PreAuthorize("hasAnyRole('PATIENT','CAREGIVER','ADMINISTRATOR')")
    public ResponseEntity<VitalSignResponse> createMeasurement(
            @RequestBody @Valid CreateVitalSignRequest request
    ) {
        // 1️⃣ Obtener identidad autenticada (desde IAM)
        var identity = authApp.getAuthenticatedIdentity();
        Long userId = identity.id();

        // 2️⃣ Crear medición asociada al usuario autenticado
        VitalSignResponse response = monitoringApp.createMeasurement(request, userId);

        // 3️⃣ Retornar resultado HTTP 201 con ubicación del recurso
        return ResponseEntity
                .created(URI.create("/monitoring/measurements/" + response.id()))
                .body(response);
    }

    @Operation(
            summary = "Get all measurements of a patient",
            description = "Returns all historical vital sign records for a given patient, sorted by most recent first.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "List of measurements",
                    content = @Content(schema = @Schema(implementation = VitalSignResponse.class))
            )
    )
    @GetMapping("/patients/{id}/measurements")
    @PreAuthorize("hasAnyRole('CAREGIVER','ADMINISTRATOR','PATIENT')")
    public ResponseEntity<List<VitalSignResponse>> getByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(monitoringApp.getMeasurementsByPatient(id));
    }

    @Operation(
            summary = "Get most recent measurements",
            description = "Fetches the N most recent measurements system-wide (useful for dashboards).",
            parameters = @Parameter(name = "limit", description = "Number of recent records to fetch", example = "20"),
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "List of recent measurements",
                    content = @Content(schema = @Schema(implementation = VitalSignResponse.class))
            )
    )
    @GetMapping("/measurements/recent")
    @PreAuthorize("hasAnyRole('ADMINISTRATOR')")
    public ResponseEntity<List<VitalSignResponse>> getRecent(@RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(monitoringApp.getRecentMeasurements(limit));
    }

    @Operation(
            summary = "Get measurement by ID",
            description = "Fetches a single vital sign record by its unique identifier.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Measurement found",
                            content = @Content(schema = @Schema(implementation = VitalSignResponse.class))),
                    @ApiResponse(responseCode = "404", description = "Measurement not found")
            }
    )
    @GetMapping("/measurements/{id}")
    @PreAuthorize("hasAnyRole('CAREGIVER','ADMINISTRATOR', 'PATIENT')")
    public ResponseEntity<VitalSignResponse> getMeasurementById(@PathVariable Long id) {
        return ResponseEntity.ok(monitoringApp.getMeasurementById(id));
    }

    @Operation(
            summary = "Get alerts for a specific patient",
            description = "Returns all alerts generated for a given patient, ordered from most recent to oldest.",
            responses = @ApiResponse(
                    responseCode = "200",
                    description = "List of alerts",
                    content = @Content(schema = @Schema(implementation = AlertResponse.class))
            )
    )
    @GetMapping("/patients/{id}/alerts")
    @PreAuthorize("hasAnyRole('PATIENT','CAREGIVER','ADMINISTRATOR')")
    public ResponseEntity<List<AlertResponse>> getAlertsByPatient(@PathVariable Long id) {
        return ResponseEntity.ok(alertApp.getAlertsByPatient(id));
    }

    @Operation(
            summary = "Subscribe to real-time monitoring stream",
            description = """
        Opens a Server-Sent Events (SSE) channel that pushes real-time data updates
        (vital signs and alerts) for the specified user (patient or caregiver).
        """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "SSE stream started"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized access")
            }
    )
    @GetMapping(value = "/stream/user/{userId}", produces = "text/event-stream")
    public SseEmitter subscribeRealtime(@PathVariable Long userId) {
        return emitterService.subscribe(userId);
    }
}



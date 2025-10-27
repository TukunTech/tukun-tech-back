package com.upc.tukuntech.backend.modules.monitoring.application.service;

import com.upc.tukuntech.backend.modules.monitoring.application.commands.CreateVitalSignCommand;
import com.upc.tukuntech.backend.modules.monitoring.application.commands.handlers.CreateVitalSignCommandHandler;
import com.upc.tukuntech.backend.modules.monitoring.application.dto.CreateVitalSignRequest;
import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.mapper.VitalSignMapper;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.GetMeasurementsByPatientQuery;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.GetRecentMeasurementsQuery;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.handlers.GetMeasurementsByPatientQueryHandler;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.handlers.GetRecentMeasurementsQueryHandler;
import com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.HeartRate;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.OxygenLevel;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.Temperature;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import com.upc.tukuntech.backend.modules.monitoring.infrastructure.realtime.SseEmitterService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class MonitoringApplicationService {

    private final CreateVitalSignCommandHandler createHandler;
    private final GetMeasurementsByPatientQueryHandler getByPatientHandler;
    private final GetRecentMeasurementsQueryHandler getRecentHandler;
    private final AlertDomainService alertDomainService;
    private final SseEmitterService emitterService;
    private final VitalSignRecordRepository recordRepo;
    private final VitalSignMapper vitalSignMapper;


    public MonitoringApplicationService(
            CreateVitalSignCommandHandler createHandler,
            GetMeasurementsByPatientQueryHandler getByPatientHandler,
            GetRecentMeasurementsQueryHandler getRecentHandler,
            AlertDomainService alertDomainService,
            SseEmitterService emitterService,
            VitalSignRecordRepository recordRepo,
            VitalSignMapper vitalSignMapper
    ) {
        this.createHandler = createHandler;
        this.getByPatientHandler = getByPatientHandler;
        this.getRecentHandler = getRecentHandler;
        this.alertDomainService = alertDomainService;
        this.emitterService = emitterService;
        this.recordRepo = recordRepo;
        this.vitalSignMapper = vitalSignMapper;
    }

    /**
     * Crea una nueva medición y analiza automáticamente si hay valores fuera de rango.
     */
    public VitalSignResponse createMeasurement(CreateVitalSignRequest request) {
        // 1️⃣ Ejecutar comando → guardar medición en BD
        VitalSignResponse response = createHandler.handle(
                request.patientId(),
                request.deviceId(),
                request.heartRate(),
                request.oxygenLevel(),
                request.temperature()
        );

        // 2️⃣ Reconstruir entidad de dominio desde la request
        VitalSignRecord record = VitalSignRecord.create(
                request.patientId(),
                request.deviceId(),
                new HeartRate(request.heartRate()),
                new OxygenLevel(request.oxygenLevel()),
                new Temperature(request.temperature())
        );

        // 3️⃣ Procesar posibles alertas de dominio
        alertDomainService.processMeasurement(record);

        // 4️⃣ Emitir evento SSE de la medición al paciente
        emitterService.emitVitalSign(record);

        return response;
    }

    /**
     * Devuelve todas las mediciones históricas de un paciente.
     */
    public List<VitalSignResponse> getMeasurementsByPatient(Long patientId) {
        var query = new GetMeasurementsByPatientQuery(patientId);
        return getByPatientHandler.handle(query);
    }

    /**
     * Devuelve las mediciones más recientes (para dashboards en tiempo real).
     */
    public List<VitalSignResponse> getRecentMeasurements(int limit) {
        var query = new GetRecentMeasurementsQuery(limit);
        return getRecentHandler.handle(query);
    }

    public VitalSignResponse getMeasurementById(Long id) {
        var record = recordRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Measurement not found with id " + id
                ));

        return vitalSignMapper.toResponse(record);
    }
}

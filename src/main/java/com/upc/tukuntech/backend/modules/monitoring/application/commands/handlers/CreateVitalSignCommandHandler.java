package com.upc.tukuntech.backend.modules.monitoring.application.commands.handlers;

import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.mapper.AlertMapper;
import com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.HeartRate;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.OxygenLevel;
import com.upc.tukuntech.backend.modules.monitoring.domain.model.valueobject.Temperature;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.AlertRepository;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import com.upc.tukuntech.backend.modules.monitoring.infrastructure.realtime.SseEmitterService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Command Handler responsable de crear un nuevo registro de signos vitales.
 * Solo ejecuta la operación principal del caso de uso (persistencia).
 * La lógica de alertas y SSE se maneja en capas superiores.
 */
@Component
public class CreateVitalSignCommandHandler {

    private final VitalSignRecordRepository recordRepo;

    public CreateVitalSignCommandHandler(VitalSignRecordRepository recordRepo) {
        this.recordRepo = recordRepo;
    }

    @Transactional
    public VitalSignResponse handle(Long patientId, Long deviceId, Integer hr, Integer spo2, Double temp) {
        if (patientId == null || deviceId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient and Device IDs are required");

        // 1️⃣ Crear entidad de dominio con Value Objects
        VitalSignRecord record = VitalSignRecord.create(
                patientId,
                deviceId,
                new HeartRate(hr),
                new OxygenLevel(spo2),
                new Temperature(temp)
        );

        // 2️⃣ Persistir registro
        VitalSignRecord saved = recordRepo.save(record);

        // 3️⃣ Construir respuesta DTO
        return new VitalSignResponse(
                saved.getId(),
                saved.getPatientId(),
                saved.getDeviceId(),
                saved.getHeartRate().getValue(),
                saved.getOxygenLevel().getValue(),
                saved.getTemperature().getValue(),
                saved.getTimestamp()
        );
    }
}

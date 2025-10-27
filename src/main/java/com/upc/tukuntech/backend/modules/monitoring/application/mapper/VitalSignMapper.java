package com.upc.tukuntech.backend.modules.monitoring.application.mapper;

import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades de dominio VitalSignRecord en DTOs VitalSignResponse.
 * Mantiene consistencia con otros mappers (como AlertMapper).
 */
@Component
public class VitalSignMapper {

    /**
     * Convierte una entidad de dominio VitalSignRecord a su DTO VitalSignResponse.
     */
    public VitalSignResponse toResponse(VitalSignRecord record) {
        if (record == null) return null;

        return new VitalSignResponse(
                record.getId(),
                record.getPatientId(),
                record.getDeviceId(),
                record.getHeartRate() != null ? record.getHeartRate().getValue() : null,
                record.getOxygenLevel() != null ? record.getOxygenLevel().getValue() : null,
                record.getTemperature() != null ? record.getTemperature().getValue() : null,
                record.getTimestamp()
        );
    }


}

package com.upc.tukuntech.backend.modules.monitoring.application.mapper;


import com.upc.tukuntech.backend.modules.monitoring.application.dto.AlertResponse;
import com.upc.tukuntech.backend.modules.monitoring.domain.entity.Alert;
import org.springframework.stereotype.Component;

/**
 * Mapper para convertir entidades de dominio Alert a DTOs de capa de aplicación.
 * Mantiene consistencia con otros mappers (naming y estructura).
 */
@Component
public class AlertMapper {

    /**
     * Convierte una entidad Alert en un DTO AlertResponse.
     */
    public AlertResponse toResponse(Alert alert) {
        if (alert == null) return null;
        return new AlertResponse(
                alert.getId(),
                alert.getPatientId(),
                alert.getDeviceId(),
                alert.getType(),
                alert.getSeverity(),
                alert.getMessage(),
                alert.getCreatedAt()
        );
    }


}

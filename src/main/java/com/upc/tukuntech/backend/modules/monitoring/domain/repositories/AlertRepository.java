package com.upc.tukuntech.backend.modules.monitoring.domain.repositories;

import com.upc.tukuntech.backend.modules.monitoring.domain.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio de dominio para gestionar las alertas generadas por signos vitales anómalos.
 */
@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Devuelve todas las alertas asociadas a un paciente.
     */
    List<Alert> findByPatientId(Long patientId);

    /**
     * Devuelve todas las alertas filtradas por nivel de severidad.
     */
    List<Alert> findBySeverityIgnoreCase(String severity);

    /**
     * Devuelve todas las alertas creadas dentro de un rango temporal.
     */
    List<Alert> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Devuelve las alertas más recientes (limitadas por tiempo o id).
     */
    List<Alert> findTop20ByOrderByCreatedAtDesc();
}
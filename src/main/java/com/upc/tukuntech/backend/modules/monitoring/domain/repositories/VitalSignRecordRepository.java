package com.upc.tukuntech.backend.modules.monitoring.domain.repositories;

import com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de dominio para acceder a registros de signos vitales.
 * Encapsula consultas comunes dentro del contexto de monitoreo.
 */
@Repository
public interface VitalSignRecordRepository extends JpaRepository<VitalSignRecord, Long> {

    /**
     * Devuelve todos los registros de un paciente, más recientes primero.
     */
    List<VitalSignRecord> findByPatientIdOrderByTimestampDesc(Long patientId);

    /**
     * Devuelve todas las mediciones asociadas a un dispositivo.
     */
    List<VitalSignRecord> findByDeviceId(Long deviceId);

    /**
     * Devuelve las mediciones realizadas dentro de un rango de tiempo.
     */
    List<VitalSignRecord> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Devuelve el último registro de un paciente (lectura más reciente).
     */
    Optional<VitalSignRecord> findFirstByPatientIdOrderByTimestampDesc(Long patientId);

    /**
     * Devuelve los registros más recientes (limit N) — útil para dashboards.
     */
    @Query("SELECT v FROM VitalSignRecord v ORDER BY v.timestamp DESC LIMIT ?1")
    List<VitalSignRecord> findRecent(int limit);
}

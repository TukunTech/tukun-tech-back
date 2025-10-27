package com.upc.tukuntech.backend.modules.monitoring.application.service;

import com.upc.tukuntech.backend.modules.monitoring.application.dto.AlertResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.mapper.AlertMapper;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.AlertRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service para gestionar la lectura y exposición de alertas.
 * Solo ejecuta consultas y mapeos; la lógica de generación está en el dominio.
 */
@Service
public class AlertApplicationService {

    private final AlertRepository repository;
    private final AlertMapper mapper;

    public AlertApplicationService(AlertRepository repository, AlertMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    /**
     * Devuelve las alertas más recientes de un paciente.
     */
    public List<AlertResponse> getAlertsByPatient(Long patientId) {
        return repository.findByPatientId(patientId)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt())) // más recientes primero
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve las alertas recientes (últimas N horas) para paneles o dashboards.
     */
    public List<AlertResponse> getRecentAlerts(int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return repository.findAll().stream()
                .filter(a -> a.getCreatedAt().isAfter(cutoff))
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Devuelve las alertas filtradas por severidad.
     */
    public List<AlertResponse> getAlertsBySeverity(String severity) {
        return repository.findBySeverityIgnoreCase(severity)
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }
}

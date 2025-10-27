package com.upc.tukuntech.backend.modules.monitoring.application.queries.handlers;

import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.mapper.VitalSignMapper;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.GetMeasurementsByPatientQuery;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Query Handler responsable de obtener las mediciones de un paciente específico.
 */
@Component
public class GetMeasurementsByPatientQueryHandler {

    private final VitalSignRecordRepository repository;
    private final VitalSignMapper mapper;

    public GetMeasurementsByPatientQueryHandler(VitalSignRecordRepository repository,
                                                VitalSignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<VitalSignResponse> handle(GetMeasurementsByPatientQuery query) {
        return repository.findByPatientIdOrderByTimestampDesc(query.patientId())
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}
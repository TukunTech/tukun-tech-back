package com.upc.tukuntech.backend.modules.monitoring.application.queries.handlers;

import com.upc.tukuntech.backend.modules.monitoring.application.dto.VitalSignResponse;
import com.upc.tukuntech.backend.modules.monitoring.application.mapper.VitalSignMapper;
import com.upc.tukuntech.backend.modules.monitoring.application.queries.GetRecentMeasurementsQuery;
import com.upc.tukuntech.backend.modules.monitoring.domain.repositories.VitalSignRecordRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Query Handler responsable de obtener las mediciones más recientes,
 * para dashboards o paneles en tiempo real.
 */
@Component
public class GetRecentMeasurementsQueryHandler {

    private final VitalSignRecordRepository repository;
    private final VitalSignMapper mapper;

    public GetRecentMeasurementsQueryHandler(VitalSignRecordRepository repository,
                                             VitalSignMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<VitalSignResponse> handle(GetRecentMeasurementsQuery query) {
        // Usa el método custom findRecent() si existe; de lo contrario, fallback a findAll con paginación
        List<com.upc.tukuntech.backend.modules.monitoring.domain.entity.VitalSignRecord> records;

        try {
            records = repository.findRecent(query.limit());
        } catch (Exception e) {
            // fallback si la BD no soporta LIMIT en @Query
            records = repository.findAll()
                    .stream()
                    .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                    .limit(query.limit())
                    .toList();
        }

        return records.stream()
                .map(mapper::toResponse)
                .toList();
    }
}

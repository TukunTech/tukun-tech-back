package com.upc.tukuntech.backend.modules.caremanagement.application.queries.handlers;

import com.upc.tukuntech.backend.modules.caremanagement.application.queries.GetCaregiversByPatientQuery;
import com.upc.tukuntech.backend.modules.caremanagement.domain.entity.CareAssignment;
import com.upc.tukuntech.backend.modules.caremanagement.domain.repositories.CareAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetCaregiversByPatientQueryHandler {

    private final CareAssignmentRepository repository;

    public GetCaregiversByPatientQueryHandler(CareAssignmentRepository repository) {
        this.repository = repository;
    }

    public List<Long> handle(GetCaregiversByPatientQuery query) {
        return repository.findByPatientId(query.patientId())
                .stream()
                .map(CareAssignment::getCaregiverId)
                .toList();
    }
}

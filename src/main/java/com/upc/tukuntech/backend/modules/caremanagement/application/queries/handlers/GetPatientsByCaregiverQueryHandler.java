package com.upc.tukuntech.backend.modules.caremanagement.application.queries.handlers;

import com.upc.tukuntech.backend.modules.caremanagement.application.queries.GetPatientsByCaregiverQuery;
import com.upc.tukuntech.backend.modules.caremanagement.domain.entity.CareAssignment;
import com.upc.tukuntech.backend.modules.caremanagement.domain.repositories.CareAssignmentRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetPatientsByCaregiverQueryHandler {

    private final CareAssignmentRepository repository;

    public GetPatientsByCaregiverQueryHandler(CareAssignmentRepository repository) {
        this.repository = repository;
    }

    public List<Long> handle(GetPatientsByCaregiverQuery query) {
        return repository.findByCaregiverId(query.caregiverId())
                .stream()
                .map(CareAssignment::getPatientId)
                .toList();
    }
}

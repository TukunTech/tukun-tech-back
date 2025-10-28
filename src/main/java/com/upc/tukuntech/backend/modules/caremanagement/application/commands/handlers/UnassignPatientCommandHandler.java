package com.upc.tukuntech.backend.modules.caremanagement.application.commands.handlers;

import com.upc.tukuntech.backend.modules.caremanagement.application.commands.UnassignPatientCommand;
import com.upc.tukuntech.backend.modules.caremanagement.domain.repositories.CareAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UnassignPatientCommandHandler {

    private final CareAssignmentRepository repository;

    public UnassignPatientCommandHandler(CareAssignmentRepository repository) {
        this.repository = repository;
    }

    public void handle(UnassignPatientCommand command) {
        var assignment = repository.findByCaregiverIdAndPatientId(
                command.caregiverId(), command.patientId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No existe asignación entre este cuidador y paciente."
        ));

        repository.delete(assignment);
    }
}

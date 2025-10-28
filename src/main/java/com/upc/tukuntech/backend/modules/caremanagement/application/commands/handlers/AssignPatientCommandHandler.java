package com.upc.tukuntech.backend.modules.caremanagement.application.commands.handlers;

import com.upc.tukuntech.backend.modules.caremanagement.application.commands.AssignPatientCommand;
import com.upc.tukuntech.backend.modules.caremanagement.domain.entity.CareAssignment;
import com.upc.tukuntech.backend.modules.caremanagement.domain.repositories.CareAssignmentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AssignPatientCommandHandler {

    private final CareAssignmentRepository repository;

    public AssignPatientCommandHandler(CareAssignmentRepository repository) {
        this.repository = repository;
    }

    public void handle(AssignPatientCommand command) {
        // Verificar si ya existe
        boolean exists = repository.findByCaregiverIdAndPatientId(
                command.caregiverId(), command.patientId()
        ).isPresent();

        if (exists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El paciente ya está asignado a este cuidador."
            );
        }

        // Crear y guardar la asignación
        CareAssignment assignment = CareAssignment.assign(
                command.caregiverId(),
                command.patientId()
        );

        repository.save(assignment);
    }
}

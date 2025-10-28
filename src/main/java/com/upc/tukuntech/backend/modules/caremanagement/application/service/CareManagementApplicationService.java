package com.upc.tukuntech.backend.modules.caremanagement.application.service;

import com.upc.tukuntech.backend.modules.caremanagement.application.commands.AssignPatientCommand;
import com.upc.tukuntech.backend.modules.caremanagement.application.commands.UnassignPatientCommand;
import com.upc.tukuntech.backend.modules.caremanagement.application.commands.handlers.AssignPatientCommandHandler;
import com.upc.tukuntech.backend.modules.caremanagement.application.commands.handlers.UnassignPatientCommandHandler;
import com.upc.tukuntech.backend.modules.caremanagement.application.queries.GetCaregiversByPatientQuery;
import com.upc.tukuntech.backend.modules.caremanagement.application.queries.GetPatientsByCaregiverQuery;
import com.upc.tukuntech.backend.modules.caremanagement.application.queries.handlers.GetCaregiversByPatientQueryHandler;
import com.upc.tukuntech.backend.modules.caremanagement.application.queries.handlers.GetPatientsByCaregiverQueryHandler;
import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.profiles.application.facade.ProfileApplicationFacade;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CareManagementApplicationService {

    private final AuthApplicationService authApp;
    private final ProfileApplicationFacade profileApp;
    private final AssignPatientCommandHandler assignHandler;
    private final UnassignPatientCommandHandler unassignHandler;
    private final GetPatientsByCaregiverQueryHandler patientsQueryHandler;
    private final GetCaregiversByPatientQueryHandler caregiversQueryHandler;

    public CareManagementApplicationService(
            AuthApplicationService authApp,
            ProfileApplicationFacade profileApp,
            AssignPatientCommandHandler assignHandler,
            UnassignPatientCommandHandler unassignHandler,
            GetPatientsByCaregiverQueryHandler patientsQueryHandler,
            GetCaregiversByPatientQueryHandler caregiversQueryHandler
    ) {
        this.authApp = authApp;
        this.profileApp = profileApp;
        this.assignHandler = assignHandler;
        this.unassignHandler = unassignHandler;
        this.patientsQueryHandler = patientsQueryHandler;
        this.caregiversQueryHandler = caregiversQueryHandler;
    }

    // 🔹 Asignar paciente al cuidador autenticado (por userId)
    public UserProfileResponse assignPatientByUserId(Long userId) {
        var identity = authApp.getAuthenticatedIdentity();
        var role = identity.roles().stream().findFirst().orElse("UNKNOWN");

        if (!(role.equals("ATTENDANT") || role.equals("ADMINISTRATOR"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo cuidadores o administradores pueden asignar pacientes.");
        }

        // ✅ Buscar el perfil del usuario (por userId)
        var patientProfile = profileApp.getProfileByUserId(userId);
        if (patientProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró un perfil asociado al usuario con ID " + userId);
        }

        // ✅ Validar tipo de perfil
        if (patientProfile.profileType() != ProfileType.PATIENT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El usuario indicado no pertenece al tipo PATIENT.");
        }

        // 🧩 Crear comando usando los IDs de perfiles (no IAM)
        var command = new AssignPatientCommand(identity.id(), patientProfile.id());
        assignHandler.handle(command);

        return patientProfile;
    }

    // 🔹 Quitar asignación paciente-cuidador (por userId)
    public void unassignPatientByUserId(Long userId) {
        var identity = authApp.getAuthenticatedIdentity();
        var role = identity.roles().stream().findFirst().orElse("UNKNOWN");

        if (!(role.equals("ATTENDANT") || role.equals("ADMINISTRATOR"))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo cuidadores o administradores pueden quitar asignaciones.");
        }

        // ✅ Buscar perfil del paciente
        var patientProfile = profileApp.getProfileByUserId(userId);
        if (patientProfile == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "No se encontró un perfil asociado al usuario con ID " + userId);
        }

        var command = new UnassignPatientCommand(identity.id(), patientProfile.id());
        unassignHandler.handle(command);
    }

    // 🔹 Obtener pacientes asignados al cuidador autenticado (con datos completos)
    public List<UserProfileResponse> getMyPatientProfiles() {
        var identity = authApp.getAuthenticatedIdentity();
        var role = identity.roles().stream().findFirst().orElse("UNKNOWN");

        if (!role.equals("ATTENDANT") && !role.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo cuidadores o administradores pueden ver pacientes asignados.");
        }

        var query = new GetPatientsByCaregiverQuery(identity.id());
        var patientIds = patientsQueryHandler.handle(query);

        return patientIds.stream()
                .map(profileApp::getProfileById)
                .filter(p -> p != null)
                .toList();
    }

    // 🔹 Obtener cuidadores asignados al paciente autenticado (con datos completos)
    public List<UserProfileResponse> getMyCaregiverProfiles() {
        var identity = authApp.getAuthenticatedIdentity();
        var role = identity.roles().stream().findFirst().orElse("UNKNOWN");

        if (!role.equals("PATIENT") && !role.equals("ADMINISTRATOR")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo pacientes o administradores pueden ver cuidadores asignados.");
        }

        var query = new GetCaregiversByPatientQuery(identity.id());
        var caregiverIds = caregiversQueryHandler.handle(query);

        return caregiverIds.stream()
                .map(profileApp::getProfileById)
                .filter(p -> p != null)
                .toList();
    }
}

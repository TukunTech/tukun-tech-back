package com.upc.tukuntech.backend.modules.profiles.application.facade;

import com.upc.tukuntech.backend.modules.iam.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.UserRepository;
import com.upc.tukuntech.backend.modules.profiles.application.commands.CreateProfileCommand;
import com.upc.tukuntech.backend.modules.profiles.application.commands.DeleteProfileByDniCommand;
import com.upc.tukuntech.backend.modules.profiles.application.commands.UpdateProfileCommand;
import com.upc.tukuntech.backend.modules.profiles.application.commands.handlers.CreateProfileCommandHandler;
import com.upc.tukuntech.backend.modules.profiles.application.commands.handlers.DeleteProfileByDniCommandHandler;
import com.upc.tukuntech.backend.modules.profiles.application.commands.handlers.UpdateProfileCommandHandler;
import com.upc.tukuntech.backend.modules.profiles.application.dto.CreateProfileRequest;
import com.upc.tukuntech.backend.modules.profiles.application.queries.GetAttendantsByFiltersQuery;
import com.upc.tukuntech.backend.modules.profiles.application.queries.GetPatientsByFiltersQuery;
import com.upc.tukuntech.backend.modules.profiles.application.queries.handlers.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProfileApplicationFacade {

    private final UserRepository userRepository;
    private final CreateProfileCommandHandler createHandler;
    private final UpdateProfileCommandHandler updateHandler;
    private final GetProfileByUserIdQueryHandler getByUserIdHandler;
    private final GetAllPatientsQueryHandler getAllPatientsHandler;
    private final GetAllAttendantsQueryHandler getAllAttendantsHandler;
    private final GetProfileByIdQueryHandler getByIdHandler;
    private final GetProfileByDniQueryHandler getByDniHandler;
    private final DeleteProfileByDniCommandHandler deleteByDniHandler;
    private final GetPatientsByFiltersQueryHandler getPatientsByFiltersHandler;
    private final GetAttendantsByFiltersQueryHandler getAttendantsByFiltersHandler;

    public ProfileApplicationFacade(
            UserRepository userRepository,
            CreateProfileCommandHandler createHandler,
            UpdateProfileCommandHandler updateHandler,
            GetProfileByUserIdQueryHandler getByUserIdHandler,
            GetAllPatientsQueryHandler getAllPatientsHandler,
            GetAllAttendantsQueryHandler getAllAttendantsHandler,
            GetProfileByIdQueryHandler getByIdHandler,
            GetProfileByDniQueryHandler getByDniHandler,
            DeleteProfileByDniCommandHandler deleteByDniHandler,
            GetPatientsByFiltersQueryHandler getPatientsByFiltersHandler,
            GetAttendantsByFiltersQueryHandler getAttendantsByFiltersHandler
    ) {
        this.userRepository = userRepository;
        this.createHandler = createHandler;
        this.updateHandler = updateHandler;
        this.getByUserIdHandler = getByUserIdHandler;
        this.getAllPatientsHandler = getAllPatientsHandler;
        this.getAllAttendantsHandler = getAllAttendantsHandler;
        this.getByIdHandler = getByIdHandler;
        this.getByDniHandler = getByDniHandler;
        this.deleteByDniHandler = deleteByDniHandler;
        this.getPatientsByFiltersHandler = getPatientsByFiltersHandler;
        this.getAttendantsByFiltersHandler = getAttendantsByFiltersHandler;
    }

    // ✅ Crear perfil (usa CommandHandler)
    public UserProfileResponse createProfile(Long userId, CreateProfileRequest request, String role) {
        var cmd = new CreateProfileCommand(
                userId,
                role,
                request.firstName(),
                request.lastName(),
                request.dni(),
                request.age(),
                request.gender(),
                request.nationality(),
                request.bloodGroup(),
                request.allergy()
        );
        return createHandler.handle(cmd);
    }

    // ✅ Actualizar perfil (usa CommandHandler)
    public UserProfileResponse updateProfile(Long userId, CreateProfileRequest request) {
        var userIdentity = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var primaryRole = userIdentity.getRoles().stream()
                .findFirst()
                .map(r -> r.getName().toUpperCase())
                .orElse("UNKNOWN");

        var cmd = new UpdateProfileCommand(
                userId,
                primaryRole,
                request.firstName(),
                request.lastName(),
                request.dni(),
                request.age(),
                request.gender(),
                request.nationality(),
                request.bloodGroup(),
                request.allergy()
        );

        return updateHandler.handle(cmd);
    }

    public void deleteProfileByDni(String dni) {
        var cmd = new DeleteProfileByDniCommand(dni);
        deleteByDniHandler.handle(cmd);
    }

    // ✅ Consultas (usa QueryHandlers)
    public UserProfileResponse getProfileByUserId(Long userId) {
        return getByUserIdHandler.handle(userId);
    }

    public List<UserProfileResponse> getAllPatients() {
        return getAllPatientsHandler.handle();
    }

    public List<UserProfileResponse> getAllAttendants() {
        return getAllAttendantsHandler.handle();
    }

    public UserProfileResponse getProfileById(Long id) {
        return getByIdHandler.handle(id);
    }

    public UserProfileResponse getProfileByDni(String dni) {
        return getByDniHandler.handle(dni);
    }

    public List<UserProfileResponse> searchPatientsByName(String name) {
        var query = new GetPatientsByFiltersQuery(name);
        return getPatientsByFiltersHandler.handle(query);
    }

    public List<UserProfileResponse> searchAttendantsByName(String name) {
        var query = new GetAttendantsByFiltersQuery(name);
        return getAttendantsByFiltersHandler.handle(query);
    }
}

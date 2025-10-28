package com.upc.tukuntech.backend.modules.profiles.application.commands.handlers;

import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.UserRepository;
import com.upc.tukuntech.backend.modules.profiles.application.commands.UpdateProfileCommand;
import com.upc.tukuntech.backend.modules.profiles.application.mapper.ProfileMapper;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class UpdateProfileCommandHandler {

    private final UserProfileRepository repository;
    private final UserRepository userRepository;
    private final ProfileMapper mapper;

    public UpdateProfileCommandHandler(UserProfileRepository repository,
                                       UserRepository userRepository,
                                       ProfileMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public UserProfileResponse handle(UpdateProfileCommand cmd) {
        var existing = repository.findByUserId(cmd.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        // 🔒 Validar rol permitido
        if (!"PATIENT".equalsIgnoreCase(cmd.role()) && !"ATTENDANT".equalsIgnoreCase(cmd.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients or attendants can update profiles");
        }

        // 🧩 Validar formato del DNI
        if (!cmd.dni().matches("\\d{8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid DNI format (must be 8 digits)");
        }

        // 🧩 Verificar si otro perfil ya tiene ese DNI
        repository.findByDni(cmd.dni()).ifPresent(existingWithSameDni -> {
            if (!existingWithSameDni.getId().equals(existing.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DNI already registered in another profile");
            }
        });

        // 🧩 Validar edad (debe estar en rango 0–120)
        if (cmd.age() == null || cmd.age() < 0 || cmd.age() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid age (must be between 0 and 120)");
        }

        // 🧩 Validar que un cuidador no intente actualizar campos clínicos
        if ("ATTENDANT".equalsIgnoreCase(cmd.role()) &&
                (cmd.bloodGroup() != null || cmd.allergy() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Attendants cannot modify clinical fields (blood group, allergy)");
        }

        // 🔹 Actualizar campos comunes
        existing.setFirstName(cmd.firstName());
        existing.setLastName(cmd.lastName());
        existing.setDni(cmd.dni());
        existing.setAge(cmd.age());
        existing.setGender(cmd.gender());
        existing.setNationality(cmd.nationality());

        // 🔹 Reglas específicas por rol
        if ("PATIENT".equalsIgnoreCase(cmd.role())) {
            existing.setBloodGroup(cmd.bloodGroup());
            existing.setAllergy(cmd.allergy());
            existing.setProfileType(ProfileType.PATIENT);
        } else {
            existing.setBloodGroup(null);
            existing.setAllergy(null);
            existing.setProfileType(ProfileType.ATTENDANT);
        }

        var updated = repository.save(existing);
        return mapper.toResponse(updated);
    }
}

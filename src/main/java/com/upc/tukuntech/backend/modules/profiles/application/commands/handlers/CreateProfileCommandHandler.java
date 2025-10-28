package com.upc.tukuntech.backend.modules.profiles.application.commands.handlers;

import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.UserRepository;
import com.upc.tukuntech.backend.modules.profiles.application.commands.CreateProfileCommand;
import com.upc.tukuntech.backend.modules.profiles.application.mapper.ProfileMapper;
import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class CreateProfileCommandHandler {

    private final UserProfileRepository repository;
    private final UserRepository userRepository;
    private final ProfileMapper mapper;

    public CreateProfileCommandHandler(UserProfileRepository repository,
                                       UserRepository userRepository,
                                       ProfileMapper mapper) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional
    public UserProfileResponse handle(CreateProfileCommand cmd) {

        // 🔹 Validaciones previas
        if (repository.findByUserId(cmd.userId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile already exists for this user");
        }

        if (!"PATIENT".equalsIgnoreCase(cmd.role()) && !"ATTENDANT".equalsIgnoreCase(cmd.role())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only patients or attendants can create profiles");
        }

        if (repository.findByDni(cmd.dni()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "DNI already registered in another profile");
        }

        if (!cmd.dni().matches("\\d{8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid DNI format (must be 8 digits)");
        }

        if (cmd.age() == null || cmd.age() < 0 || cmd.age() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid age (must be between 0 and 120)");
        }

        if ("ATTENDANT".equalsIgnoreCase(cmd.role()) &&
                (cmd.bloodGroup() != null || cmd.allergy() != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Attendants cannot include clinical fields (blood group, allergy)");
        }

        // 🔹 Crear entidad
        UserProfile profile = new UserProfile();
        profile.setUserId(cmd.userId());
        profile.setFirstName(cmd.firstName());
        profile.setLastName(cmd.lastName());
        profile.setDni(cmd.dni());
        profile.setAge(cmd.age());
        profile.setGender(cmd.gender());
        profile.setNationality(cmd.nationality());

        // 🔹 Asignar tipo de perfil según rol
        if ("PATIENT".equalsIgnoreCase(cmd.role())) {
            profile.setProfileType(ProfileType.PATIENT);
            profile.setBloodGroup(cmd.bloodGroup());
            profile.setAllergy(cmd.allergy());
        } else {
            profile.setProfileType(ProfileType.ATTENDANT);
            profile.setBloodGroup(null);
            profile.setAllergy(null);
        }

        var saved = repository.save(profile);
        return mapper.toResponse(saved);
    }
}

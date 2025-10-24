package com.upc.tukuntech.backend.modules.profiles.application.service;

import com.upc.tukuntech.backend.modules.iam.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.application.dto.CreateProfileRequest;
import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProfileApplicationService {

    private final UserProfileRepository repository;

    public ProfileApplicationService(UserProfileRepository repository) {
        this.repository = repository;
    }

    // Crear perfil después del registro en IAM
    public UserProfileResponse createProfile(Long userId, CreateProfileRequest request) {
        if (repository.findByUserId(userId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Profile already exists for this user");
        }

        UserProfile profile = new UserProfile();
        profile.setUserId(userId);
        profile.setFirstName(request.firstName());
        profile.setLastName(request.lastName());
        profile.setDni(request.dni());
        profile.setAge(request.age());
        profile.setGender(request.gender());
        profile.setBloodGroup(request.bloodGroup());
        profile.setNationality(request.nationality());
        profile.setAllergy(request.allergy());

        UserProfile saved = repository.save(profile);
        return new UserProfileResponse(
                saved.getId().toString(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getDni(),
                saved.getAge(),
                saved.getGender().name(),
                saved.getBloodGroup().name(),
                saved.getNationality().name(),
                saved.getAllergy() != null ? saved.getAllergy().name() : null
        );
    }

    // Obtener perfil del usuario autenticado
    public UserProfileResponse getProfileByUserId(Long userId) {
        return repository.findByUserId(userId)
                .map(profile -> new UserProfileResponse(
                        profile.getId().toString(),
                        profile.getFirstName(),
                        profile.getLastName(),
                        profile.getDni(),
                        profile.getAge(),
                        profile.getGender().name(),
                        profile.getBloodGroup().name(),
                        profile.getNationality().name(),
                        profile.getAllergy() != null ? profile.getAllergy().name() : null
                ))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
    }
}

package com.upc.tukuntech.backend.modules.profiles.application.queries.handlers;

import com.upc.tukuntech.backend.modules.iam.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GetProfileByIdQueryHandler {

    private final UserProfileRepository repository;

    public GetProfileByIdQueryHandler(UserProfileRepository repository) {
        this.repository = repository;
    }

    public UserProfileResponse handle(Long id) {
        var profile = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));

        return new UserProfileResponse(
                profile.getId().toString(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getDni(),
                profile.getAge(),
                profile.getGender() != null ? profile.getGender().name() : null,
                profile.getBloodGroup() != null ? profile.getBloodGroup().name() : null,
                profile.getNationality() != null ? profile.getNationality().name() : null,
                profile.getAllergy() != null ? profile.getAllergy().name() : null
        );
    }
}

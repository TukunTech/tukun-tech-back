package com.upc.tukuntech.backend.modules.profiles.application.queries.handlers;

import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.application.mapper.ProfileMapper;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class GetProfileByDniQueryHandler {

    private final UserProfileRepository repository;
    private final ProfileMapper mapper;

    public GetProfileByDniQueryHandler(UserProfileRepository repository,
                                       ProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public UserProfileResponse handle(String dni) {
        var profile = repository.findByDni(dni)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Profile not found"));
        return mapper.toResponse(profile);
    }
}

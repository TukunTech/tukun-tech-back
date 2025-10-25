package com.upc.tukuntech.backend.modules.profiles.application.queries.handlers;

import com.upc.tukuntech.backend.modules.iam.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.application.mapper.ProfileMapper;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAllPatientsQueryHandler {

    private final UserProfileRepository repository;
    private final ProfileMapper mapper;

    public GetAllPatientsQueryHandler(UserProfileRepository repository,
                                      ProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UserProfileResponse> handle() {
        return repository.findByProfileType(ProfileType.PATIENT)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}


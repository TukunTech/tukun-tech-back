package com.upc.tukuntech.backend.modules.profiles.application.queries.handlers;

import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.application.mapper.ProfileMapper;
import com.upc.tukuntech.backend.modules.profiles.application.queries.GetAttendantsByFiltersQuery;
import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import com.upc.tukuntech.backend.modules.profiles.domain.repositories.UserProfileRepository;
import com.upc.tukuntech.backend.modules.profiles.domain.specifications.UserProfileSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GetAttendantsByFiltersQueryHandler {

    private final UserProfileRepository repository;
    private final ProfileMapper mapper;

    public GetAttendantsByFiltersQueryHandler(UserProfileRepository repository, ProfileMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UserProfileResponse> handle(GetAttendantsByFiltersQuery query) {

        // 🔹 Filtro base: solo perfiles tipo ATTENDANT
        Specification<UserProfile> spec = (root, q, cb) ->
                cb.equal(root.get("profileType"), ProfileType.ATTENDANT);

        // 🔹 Si hay nombre, buscar parcial en nombre o apellido
        if (query.name() != null && !query.name().isBlank()) {
            spec = spec.and(
                    UserProfileSpecifications.hasFirstNameContaining(query.name())
                            .or(UserProfileSpecifications.hasLastNameContaining(query.name()))
            );
        }

        // 🔹 Ejecutar búsqueda
        return repository.findAll(spec)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}

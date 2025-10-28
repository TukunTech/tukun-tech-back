package com.upc.tukuntech.backend.modules.profiles.application.dto;

import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;

public record UserProfileResponse(
        Long id,
        Long userId,
        String firstName,
        String lastName,
        String dni,
        Integer age,
        String gender,
        String bloodGroup,
        String nationality,
        String allergy,
        ProfileType profileType
) {}

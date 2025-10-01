package com.upc.tukuntech.backend.modules.auth.application.dto;

public record UserProfileResponse(
        String id,
        String firstName,
        String lastName,
        String dni,
        Integer age,
        String gender,
        String bloodGroup,
        String nationality,
        String allergy
) {}

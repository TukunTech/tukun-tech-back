package com.upc.tukuntech.backend.modules.auth.application.dto;

import com.upc.tukuntech.backend.modules.auth.domain.model.Allergy;
import com.upc.tukuntech.backend.modules.auth.domain.model.BloodGroup;
import com.upc.tukuntech.backend.modules.auth.domain.model.Gender;
import com.upc.tukuntech.backend.modules.auth.domain.model.Nationality;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 8, max = 20) String dni,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 120) String password,
        @NotBlank String role,
        @NotNull Gender gender,
        @NotNull @Min(0) @Max(120) Integer age,
        @NotNull BloodGroup bloodGroup,
        @NotNull Nationality nationality,
        Allergy allergy

        ) {}

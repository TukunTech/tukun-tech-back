package com.upc.tukuntech.backend.modules.profiles.application.dto;

import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Allergy;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.BloodGroup;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Gender;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Nationality;
import jakarta.validation.constraints.*;

public record CreateProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 8, max = 20) String dni,
        @NotNull @Min(0) @Max(120) Integer age,
        @NotNull Gender gender,
        @NotNull BloodGroup bloodGroup,
        @NotNull Nationality nationality,
        Allergy allergy
) {}

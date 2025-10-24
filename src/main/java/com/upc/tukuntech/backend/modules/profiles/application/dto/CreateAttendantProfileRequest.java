package com.upc.tukuntech.backend.modules.profiles.application.dto;

import jakarta.validation.constraints.*;

public record CreateAttendantProfileRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 8, max = 20) String dni,
        @NotNull @Min(18) @Max(100) Integer age,
        @NotBlank String phoneNumber

) {}

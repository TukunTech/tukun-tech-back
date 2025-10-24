package com.upc.tukuntech.backend.modules.iam.application.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 120) String password,
        @NotBlank String role
) {}

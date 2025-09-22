package com.upc.tukuntech.backend.modules.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Size(min = 8, max = 20) String dni,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6, max = 120) String password,
        @NotBlank String role // "PATIENT" o "ATTENDANT"
) {}

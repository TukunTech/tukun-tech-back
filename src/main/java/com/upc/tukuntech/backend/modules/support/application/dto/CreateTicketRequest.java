package com.upc.tukuntech.backend.modules.support.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTicketRequest(

        @NotBlank @Size(min = 3, max = 100)
        String name,

        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 5, max = 100)
        String subject,

        @NotBlank @Size(min = 10, max = 1000)
        String description
) {}

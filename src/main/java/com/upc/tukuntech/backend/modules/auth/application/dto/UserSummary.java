package com.upc.tukuntech.backend.modules.auth.application.dto;

import java.util.Set;

public record UserSummary(
        Long id,
        String email,
        Set<String> roles
) {}

package com.upc.tukuntech.backend.modules.profiles.application.commands;

import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Allergy;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.BloodGroup;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Gender;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.Nationality;

public record CreateProfileCommand(
        Long userId,
        String role, // "PATIENT" | "ATTENDANT"
        String firstName,
        String lastName,
        String dni,
        Integer age,
        Gender gender,
        Nationality nationality,
        BloodGroup bloodGroup, // null si ATTENDANT
        Allergy allergy        // null si ATTENDANT
) {}

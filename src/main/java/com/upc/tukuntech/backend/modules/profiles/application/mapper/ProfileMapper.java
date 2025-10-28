package com.upc.tukuntech.backend.modules.profiles.application.mapper;

import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class ProfileMapper {

    public UserProfileResponse toResponse(UserProfile p) {
        return new UserProfileResponse(
                p.getId(),
                p.getUserId(),
                p.getFirstName(),
                p.getLastName(),
                p.getDni(),
                p.getAge(),
                p.getGender() != null ? p.getGender().name() : null,
                p.getBloodGroup() != null ? p.getBloodGroup().name() : null,
                p.getNationality() != null ? p.getNationality().name() : null,
                p.getAllergy() != null ? p.getAllergy().name() : null,
                p.getProfileType()
        );
    }
}

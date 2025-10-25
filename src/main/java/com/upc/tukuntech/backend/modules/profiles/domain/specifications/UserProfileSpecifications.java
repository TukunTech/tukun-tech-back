package com.upc.tukuntech.backend.modules.profiles.domain.specifications;

import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import org.springframework.data.jpa.domain.Specification;

public class UserProfileSpecifications {

    public static Specification<UserProfile> hasProfileType(ProfileType type) {
        return (root, query, cb) -> cb.equal(root.get("profileType"), type);
    }

    public static Specification<UserProfile> hasFirstNameContaining(String keyword) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("firstName")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<UserProfile> hasLastNameContaining(String keyword) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("lastName")), "%" + keyword.toLowerCase() + "%");
    }

}

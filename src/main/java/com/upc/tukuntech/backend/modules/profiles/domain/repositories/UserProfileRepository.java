package com.upc.tukuntech.backend.modules.profiles.domain.repositories;

import com.upc.tukuntech.backend.modules.profiles.domain.entity.UserProfile;
import com.upc.tukuntech.backend.modules.profiles.domain.model.valueobjects.ProfileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository
        extends JpaRepository<UserProfile, Long>,
        JpaSpecificationExecutor<UserProfile> { // 👈 habilita Specifications

    Optional<UserProfile> findByUserId(Long userId);

    List<UserProfile> findByProfileType(ProfileType profileType);

    Optional<UserProfile> findByDni(String dni);
}
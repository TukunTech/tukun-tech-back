package com.upc.tukuntech.backend.modules.auth.domain.repository;

import com.upc.tukuntech.backend.modules.auth.domain.entity.SessionEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findByUserAndActiveTrue(UserEntity user);

    Optional<SessionEntity> findByIdAndActiveTrue(UUID id);

    List<SessionEntity> findByUserAndActiveTrueAndRefreshExpiresAtBefore(UserEntity user, Instant now);

    List<SessionEntity> findByUserAndActiveTrueOrderByCreatedAtAsc(UserEntity user);

    long countByUserAndActiveTrue(UserEntity user);

    Optional<SessionEntity> findByRefreshTokenHashAndActiveTrue(String refreshTokenHash);


}

package com.upc.tukuntech.backend.modules.iam.domain.repositories;

import com.upc.tukuntech.backend.modules.iam.domain.entity.SessionEntity;
import com.upc.tukuntech.backend.modules.iam.domain.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SessionRepository extends JpaRepository<SessionEntity, UUID> {
    List<SessionEntity> findByUserAndActiveTrue(UserIdentity user);

    Optional<SessionEntity> findByIdAndActiveTrue(UUID id);

    List<SessionEntity> findByUserAndActiveTrueAndRefreshExpiresAtBefore(UserIdentity user, Instant now);

    List<SessionEntity> findByUserAndActiveTrueOrderByCreatedAtAsc(UserIdentity user);

    long countByUserAndActiveTrue(UserIdentity user);

    Optional<SessionEntity> findByRefreshTokenHashAndActiveTrue(String refreshTokenHash);


}

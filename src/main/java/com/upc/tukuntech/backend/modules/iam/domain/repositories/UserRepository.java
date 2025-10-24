package com.upc.tukuntech.backend.modules.iam.domain.repositories;

import com.upc.tukuntech.backend.modules.iam.domain.entity.UserIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByEmail(String email);
}

package com.upc.tukuntech.backend.modules.auth.domain.repository;

import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}

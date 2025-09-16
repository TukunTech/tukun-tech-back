package com.upc.tukuntech.backend.modules.auth.repository;

import com.upc.tukuntech.backend.modules.auth.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByName(String name);
}

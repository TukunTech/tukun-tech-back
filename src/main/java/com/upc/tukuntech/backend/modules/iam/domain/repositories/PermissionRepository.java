package com.upc.tukuntech.backend.modules.iam.domain.repositories;

import com.upc.tukuntech.backend.modules.iam.domain.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByName(String name);
}

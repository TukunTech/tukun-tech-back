package com.upc.tukuntech.backend.modules.auth.repository;

import com.upc.tukuntech.backend.modules.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByDni(String dni);
}

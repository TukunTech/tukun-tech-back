package com.upc.tukuntech.backend.modules.auth.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Locale;

@Entity
@Table(name = "permissions")
@Getter @Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PermissionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 64)
    @NotBlank
    @Size(max = 64)
    private String name;

    @PrePersist @PreUpdate
    private void normalize() {
        if (name != null) name = name.trim().toUpperCase(Locale.ROOT);
    }
}
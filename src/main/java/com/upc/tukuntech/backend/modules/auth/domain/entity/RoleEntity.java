package com.upc.tukuntech.backend.modules.auth.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "roles",
        indexes = {@Index(name = "uk_roles_name", columnList = "name", unique = true)})
@Getter
@Setter
@ToString(exclude = "permissions")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class RoleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private int id;

    @Column(nullable = false, unique = true, length = 32)
    @NotBlank
    @Size(max = 32)
    private String name;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_role_permission", columnNames = {"role_id", "permission_id"})
    )
    private Set<PermissionEntity> permissions = new HashSet<>();

    @PrePersist
    @PreUpdate
    private void normalize() {
        if (name != null) name = name.trim().toUpperCase(Locale.ROOT);
    }
}
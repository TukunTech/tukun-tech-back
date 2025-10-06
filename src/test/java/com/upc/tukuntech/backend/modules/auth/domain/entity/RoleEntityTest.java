package com.upc.tukuntech.backend.modules.auth.domain.entity;

import jakarta.validation.ConstraintViolation;
import org.junit.jupiter.api.DisplayName;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación y comportamiento de la entidad RoleEntity")
class RoleEntityTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private RoleEntity buildValidRole() {
        RoleEntity role = new RoleEntity();
        role.setId(1);
        role.setName("ADMINISTRATOR");
        role.setPermissions(new HashSet<>());
        return role;
    }

    private PermissionEntity perm(String name) {
        PermissionEntity p = new PermissionEntity();
        p.setName(name);
        return p;
    }

    @DisplayName("Debería validar un rol cuando los datos son correctos")
    @Test
    void should_PassValidation_When_RoleIsValid() {
        // Arrange
        RoleEntity role = buildValidRole();

        // Act
        Set<ConstraintViolation<RoleEntity>> violations = validator.validate(role);

        // Assert
        assertThat(violations).isEmpty();
    }

    @DisplayName("No debería validar un rol con nombre en blanco (si @NotBlank está presente)")
    @Test
    void should_FailValidation_When_NameIsBlank() {
        // Arrange
        RoleEntity role = buildValidRole();
        role.setName("   ");

        // Act
        Set<ConstraintViolation<RoleEntity>> violations = validator.validate(role);

        // Assert
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("name"));
    }

    @DisplayName("normalize(): debería TRIM y upper-case al nombre")
    @Test
    void should_NormalizeName_When_PrePersistOrUpdate() throws Exception {
        // Arrange
        RoleEntity role = buildValidRole();
        role.setName("  admin istrator  ");
        Method normalize = RoleEntity.class.getDeclaredMethod("normalize");
        normalize.setAccessible(true);

        // Act
        normalize.invoke(role);

        // Assert
        assertThat(role.getName()).isEqualTo("ADMIN ISTRATOR".toUpperCase());
    }

    @DisplayName("El rol debería contener los permisos asignados")
    @Test
    void should_ContainAssignedPermissions() {
        // Arrange
        RoleEntity role = buildValidRole();
        PermissionEntity read = perm("PERM_USER_READ");
        PermissionEntity write = perm("PERM_USER_WRITE");

        // Act
        role.getPermissions().add(read);
        role.getPermissions().add(write);

        // Assert
        assertThat(role.getPermissions())
                .extracting(PermissionEntity::getName)
                .containsExactlyInAnyOrder("PERM_USER_READ", "PERM_USER_WRITE");
    }

    @DisplayName("No debería duplicar permisos iguales (Set + equals/hashCode por name)")
    @Test
    void should_NotDuplicateSamePermission() throws Exception {
        // Arrange
        RoleEntity role = buildValidRole();
        PermissionEntity read1 = perm("  perm_user_read ");
        PermissionEntity read2 = perm("PERM_USER_READ");

        // Act
        Method normPerm = PermissionEntity.class.getDeclaredMethod("normalize");
        normPerm.setAccessible(true);
        normPerm.invoke(read1);
        normPerm.invoke(read2);

        role.getPermissions().add(read1);
        role.getPermissions().add(read2);

        // Assert
        assertThat(role.getPermissions()).hasSize(1);
        assertThat(role.getPermissions().iterator().next().getName()).isEqualTo("PERM_USER_READ");
    }

    @DisplayName("Dos roles con el mismo ID deberían ser iguales")
    @Test
    void should_ConsiderEqual_When_SameId() {
        // Arrange
        RoleEntity a = buildValidRole(); a.setId(10);
        RoleEntity b = buildValidRole(); b.setId(10);

        // Act
        boolean areEqual = a.equals(b);
        int hashA = a.hashCode();
        int hashB = b.hashCode();

        // Assert
        assertThat(areEqual).isTrue();
        assertThat(hashA).isEqualTo(hashB);
    }

    @DisplayName("Dos roles con distinto ID no deberían ser iguales")
    @Test
    void should_ConsiderDifferent_When_DifferentId() {
        // Arrange
        RoleEntity a = buildValidRole(); a.setId(10);
        RoleEntity b = buildValidRole(); b.setId(11);

        // Act
        boolean areEqual = a.equals(b);

        // Assert
        assertThat(areEqual).isFalse();
    }

    @DisplayName("Se puede reemplazar el Set de permisos de forma segura")
    @Test
    void should_ReplacePermissionsSet_Safely() {
        // Arrange
        RoleEntity role = buildValidRole();
        Set<PermissionEntity> first = new HashSet<>();
        first.add(perm("PERM_A"));
        role.setPermissions(first);

        // Act
        Set<PermissionEntity> replacement = new HashSet<>();
        replacement.add(perm("PERM_B"));
        role.setPermissions(replacement);

        // Assert
        assertThat(role.getPermissions())
                .extracting(PermissionEntity::getName)
                .containsExactly("PERM_B");
    }
}
package com.upc.tukuntech.backend.modules.auth.domain.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación y comportamiento de PermissionEntity (AAA)")
class PermissionEntityTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private PermissionEntity buildValid() {
        PermissionEntity p = new PermissionEntity();
        p.setName("PERM_DEVICE_READ");
        return p;
    }

    @DisplayName("Debería validar una permisión válida")
    @Test
    void should_PassValidation_When_PermissionIsValid() {
        // Arrange
        PermissionEntity p = buildValid();

        // Act
        Set<ConstraintViolation<PermissionEntity>> v = validator.validate(p);

        // Assert
        assertThat(v).isEmpty();
    }

    @DisplayName("No debería validar una permisión con nombre en blanco")
    @Test
    void should_FailValidation_When_NameIsBlank() {
        // Arrange
        PermissionEntity p = buildValid();
        p.setName("   ");

        // Act
        Set<ConstraintViolation<PermissionEntity>> v = validator.validate(p);

        // Assert
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("name"));
    }

    @DisplayName("normalize(): debería TRIM y upper-case al nombre")
    @Test
    void should_NormalizeName_OnPrePersistOrUpdate() throws Exception {
        // Arrange
        PermissionEntity p = new PermissionEntity();
        p.setName("  perm_user_read ");

        Method normalize = PermissionEntity.class.getDeclaredMethod("normalize");
        normalize.setAccessible(true);

        // Act
        normalize.invoke(p);

        // Assert
        assertThat(p.getName()).isEqualTo("PERM_USER_READ");
    }

    @DisplayName("Dos permissions con el mismo name deberían ser iguales")
    @Test
    void should_BeEqual_When_SameName() throws Exception {
        // Arrange
        PermissionEntity p1 = new PermissionEntity(); p1.setName(" perm_user_read ");
        PermissionEntity p2 = new PermissionEntity(); p2.setName("PERM_USER_READ");

        Method normalize = PermissionEntity.class.getDeclaredMethod("normalize");
        normalize.setAccessible(true);
        normalize.invoke(p1);
        normalize.invoke(p2);

        // Act
        boolean equals = p1.equals(p2);

        // Assert
        assertThat(equals).isTrue();
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

}
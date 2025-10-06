package com.upc.tukuntech.backend.modules.auth.domain.entity;

import com.upc.tukuntech.backend.modules.auth.domain.model.Allergy;
import com.upc.tukuntech.backend.modules.auth.domain.model.BloodGroup;
import com.upc.tukuntech.backend.modules.auth.domain.model.Gender;
import com.upc.tukuntech.backend.modules.auth.domain.model.Nationality;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación y comportamiento de la entidad UserEntity")
class UserEntityTest {

    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();


    private UserEntity buildValidUser() {
        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setFirstName("Ana");
        user.setLastName("Pérez");
        user.setDni("12345678");
        user.setEmail("ana.perez@example.com");
        user.setPassword("$2a$10$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwxabcd12");
        user.setGender(Gender.FEMALE);
        user.setAge(25);
        user.setBloodGroup(BloodGroup.O_POSITIVE);
        user.setNationality(Nationality.PERUVIAN);
        user.setAllergy(Allergy.NONE);
        user.setEnabled(true);
        return user;
    }


    @DisplayName("Debería crear un usuario válido cuando los datos son correctos")
    @Test
    void testCreateUserWithValidData() {
        // Arrange
        UserEntity user = buildValidUser();

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations).isEmpty();
    }

    @DisplayName("No debería validar un usuario con email inválido")
    @Test
    void testUserWithInvalidEmailShouldFailValidation() {
        // Arrange
        UserEntity user = buildValidUser();
        user.setEmail("no-es-email");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
    }

    @DisplayName("No debería validar un usuario con nombres vacíos")
    @Test
    void testUserWithBlankNamesShouldFailValidation() {
        // Arrange
        UserEntity user = buildValidUser();
        user.setFirstName("   ");
        user.setLastName("");

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName"))
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName"));
    }

    @DisplayName("No debería validar un usuario con edad fuera de rango")
    @Test
    void testUserWithInvalidAgeShouldFailValidation() {
        // Arrange
        UserEntity user = buildValidUser();
        user.setAge(-5);

        // Act
        Set<ConstraintViolation<UserEntity>> violations = validator.validate(user);

        // Assert
        assertThat(violations)
                .anyMatch(v -> v.getPropertyPath().toString().equals("age"));
    }

    @DisplayName("El método normalize() debería formatear email y eliminar espacios")
    @Test
    void testNormalizeShouldTrimAndLowercaseFields() throws Exception {
        // Arrange
        UserEntity user = buildValidUser();
        user.setEmail("  ANA.PEREZ@EXAMPLE.COM ");
        user.setFirstName("  Ana  ");
        user.setLastName("  Pérez ");
        user.setDni("  12345678  ");
        Method normalizeMethod = UserEntity.class.getDeclaredMethod("normalize");
        normalizeMethod.setAccessible(true);

        // Act
        normalizeMethod.invoke(user);

        // Assert
        assertThat(user.getEmail()).isEqualTo("ana.perez@example.com");
        assertThat(user.getFirstName()).isEqualTo("Ana");
        assertThat(user.getLastName()).isEqualTo("Pérez");
        assertThat(user.getDni()).isEqualTo("12345678");
    }

    @DisplayName("El método toString() no debería mostrar el campo password")
    @Test
    void testToStringShouldNotExposePassword() {
        // Arrange
        UserEntity user = buildValidUser();

        // Act
        String userString = user.toString();

        // Assert
        assertThat(userString.toLowerCase(Locale.ROOT))
                .doesNotContain("password")
                .doesNotContain(user.getPassword());
    }

    @DisplayName("Dos usuarios con el mismo ID deberían ser iguales")
    @Test
    void testEqualsAndHashCodeWhenSameId() {
        // Arrange
        UserEntity userA = buildValidUser();
        UserEntity userB = buildValidUser();
        userA.setId(1L);
        userB.setId(1L);

        // Act
        boolean result = userA.equals(userB);

        // Assert
        assertThat(result).isTrue();
        assertThat(userA.hashCode()).isEqualTo(userB.hashCode());
    }

    @DisplayName("Dos usuarios con ID diferentes no deberían ser iguales")
    @Test
    void testEqualsAndHashCodeWhenDifferentId() {
        // Arrange
        UserEntity userA = buildValidUser();
        UserEntity userB = buildValidUser();
        userA.setId(1L);
        userB.setId(2L);

        // Act
        boolean result = userA.equals(userB);

        // Assert
        assertThat(result).isFalse();
    }
}
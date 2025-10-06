package com.upc.tukuntech.backend.modules.auth.domain.entity;

import com.upc.tukuntech.backend.modules.auth.domain.model.BloodGroup;
import com.upc.tukuntech.backend.modules.auth.domain.model.Gender;
import com.upc.tukuntech.backend.modules.auth.domain.model.Nationality;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Validación y reglas de negocio de SessionEntity")
class SessionEntityTest {
    private static final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private UserEntity stubUser(Long id) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setFirstName("Ana");
        u.setLastName("Pérez");
        u.setDni("12345678");
        u.setEmail("ana.perez@example.com");
        u.setPassword("$2a$10$abcdefghijklmnopqrstuvwxabcdefghijklmnopqrstuvwxabcd12");
        u.setGender(Gender.FEMALE);
        u.setAge(25);
        u.setBloodGroup(BloodGroup.O_POSITIVE);
        u.setNationality(Nationality.PERUVIAN);
        u.setEnabled(true);
        return u;
    }

    private SessionEntity buildValidSession() {
        SessionEntity s = new SessionEntity();
        s.setId(UUID.randomUUID());
        s.setUser(stubUser(1L));
        s.setRefreshTokenHash("refresh-hash-demo");
        s.setAccessExpiresAt(Instant.now().plusSeconds(3600));
        s.setRefreshExpiresAt(Instant.now().plusSeconds(30 * 24 * 3600));
        s.setActive(true);
        s.setIp("127.0.0.1");
        s.setUserAgent("JUnit");
        s.setCreatedAt(Instant.now());
        s.setUpdatedAt(Instant.now());
        return s;
    }

    @DisplayName("Debería pasar validación cuando la sesión es válida")
    @Test
    void should_PassValidation_When_SessionIsValid() {
        // Arrange
        SessionEntity s = buildValidSession();

        // Act
        Set<ConstraintViolation<SessionEntity>> v = validator.validate(s);

        // Assert
        assertThat(v).isEmpty();
    }

    @DisplayName("No debería validar cuando user es null")
    @Test
    void should_FailValidation_When_UserIsNull() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setUser(null);

        // Act
        Set<ConstraintViolation<SessionEntity>> v = validator.validate(s);

        // Assert
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("user"));
    }

    @DisplayName("No debería validar cuando refreshTokenHash está en blanco")
    @Test
    void should_FailValidation_When_RefreshTokenHashBlank() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setRefreshTokenHash("   ");

        // Act
        Set<ConstraintViolation<SessionEntity>> v = validator.validate(s);

        // Assert
        assertThat(v).anyMatch(cv -> cv.getPropertyPath().toString().equals("refreshTokenHash"));
    }

    @DisplayName("No debería validar cuando las fechas de expiración son null")
    @Test
    void should_FailValidation_When_ExpiresAreNull() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setAccessExpiresAt(null);
        s.setRefreshExpiresAt(null);

        // Act
        Set<ConstraintViolation<SessionEntity>> v = validator.validate(s);

        // Assert
        assertThat(v)
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("accessExpiresAt"))
                .anyMatch(cv -> cv.getPropertyPath().toString().equals("refreshExpiresAt"));
    }

    @DisplayName("isAccessExpired: debería ser true cuando accessExpiresAt ya pasó")
    @Test
    void should_ReturnTrueIsAccessExpired_When_AccessTimeInPast() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setAccessExpiresAt(Instant.now().minusSeconds(1));

        // Act
        boolean expired = s.isAccessExpired(Instant.now());

        // Assert
        assertThat(expired).isTrue();
    }

    @DisplayName("isRefreshExpired: debería ser false cuando refresh aún no expira")
    @Test
    void should_ReturnFalseIsRefreshExpired_When_RefreshInFuture() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setRefreshExpiresAt(Instant.now().plusSeconds(60));

        // Act
        boolean expired = s.isRefreshExpired(Instant.now());

        // Assert
        assertThat(expired).isFalse();
    }

    @DisplayName("canRefresh: true si sesión activa y refresh no expirado")
    @Test
    void should_AllowRefresh_When_ActiveAndRefreshValid() {
        // Arrange
        SessionEntity s = buildValidSession();
        s.setActive(true);
        s.setRefreshExpiresAt(Instant.now().plusSeconds(60));

        // Act
        boolean can = s.canRefresh(Instant.now());

        // Assert
        assertThat(can).isTrue();
    }

    @DisplayName("canRefresh: false si sesión inactiva o refresh expirado")
    @Test
    void should_DenyRefresh_When_InactiveOrRefreshExpired() {
        // Arrange
        SessionEntity inactive = buildValidSession();
        inactive.setActive(false);

        SessionEntity expired = buildValidSession();
        expired.setRefreshExpiresAt(Instant.now().minusSeconds(1));

        // Act
        boolean canInactive = inactive.canRefresh(Instant.now());
        boolean canExpired = expired.canRefresh(Instant.now());

        // Assert
        assertThat(canInactive).isFalse();
        assertThat(canExpired).isFalse();
    }

    @DisplayName("revoke: debería marcar inactiva e informar revokedAt (idempotente)")
    @Test
    void should_RevokeSession_Idempotently() {
        // Arrange
        SessionEntity s = buildValidSession();

        // Act
        s.revoke();
        Instant firstRevokedAt = s.getRevokedAt();
        boolean firstActive = s.getActive();

        s.revoke();
        Instant secondRevokedAt = s.getRevokedAt();
        boolean secondActive = s.getActive();

        // Assert
        assertThat(firstActive).isFalse();
        assertThat(secondActive).isFalse();
        assertThat(firstRevokedAt).isNotNull();
        assertThat(secondRevokedAt).isEqualTo(firstRevokedAt);
    }

    @DisplayName("@PrePersist: debería inicializar timestamps y normalizar campos")
    @Test
    void should_InitTimestampsAndNormalize_OnPrePersist() throws Exception {
        // Arrange
        SessionEntity s = new SessionEntity();
        s.setId(null);
        s.setUser(stubUser(1L));
        s.setRefreshTokenHash("  hash  ");
        s.setAccessExpiresAt(Instant.now().plusSeconds(1));
        s.setRefreshExpiresAt(Instant.now().plusSeconds(60));
        s.setActive(true);
        s.setIp("  127.0.0.1  ");
        s.setUserAgent("  junit  ");

        Method prePersist = SessionEntity.class.getDeclaredMethod("prePersist");
        prePersist.setAccessible(true);

        // Act
        prePersist.invoke(s);

        // Assert
        assertThat(s.getId()).isNotNull();
        assertThat(s.getCreatedAt()).isNotNull();
        assertThat(s.getUpdatedAt()).isNotNull();
        assertThat(s.getRefreshTokenHash()).isEqualTo("hash");
        assertThat(s.getIp()).isEqualTo("127.0.0.1");
        assertThat(s.getUserAgent()).isEqualTo("junit");
    }

    @DisplayName("@PreUpdate: debería actualizar updatedAt y normalizar campos")
    @Test
    void should_UpdateUpdatedAtAndNormalize_OnPreUpdate() throws Exception {
        // Arrange
        SessionEntity s = buildValidSession();
        Instant before = s.getUpdatedAt();
        s.setIp("  LOCALHOST ");
        s.setUserAgent("  Agent  ");
        s.setRefreshTokenHash("  token  ");

        Method preUpdate = SessionEntity.class.getDeclaredMethod("preUpdate");
        preUpdate.setAccessible(true);

        // Act
        Thread.sleep(5);
        preUpdate.invoke(s);

        // Assert
        assertThat(s.getUpdatedAt()).isAfter(before);
        assertThat(s.getIp()).isEqualTo("localhost");
        assertThat(s.getUserAgent()).isEqualTo("Agent");
        assertThat(s.getRefreshTokenHash()).isEqualTo("token");
    }

    @DisplayName("toString: no debería exponer refreshTokenHash")
    @Test
    void should_NotLeakRefreshTokenHash_InToString() {
        // Arrange
        SessionEntity s = buildValidSession();

        // Act
        String txt = s.toString();

        // Assert
        assertThat(txt).doesNotContain("refreshTokenHash")
                .doesNotContain(s.getRefreshTokenHash());
    }

    @DisplayName("equals/hashCode: dos sesiones con el mismo UUID deben ser iguales")
    @Test
    void should_ConsiderEqual_When_SameUUID() {
        // Arrange
        UUID id = UUID.randomUUID();
        SessionEntity a = buildValidSession(); a.setId(id);
        SessionEntity b = buildValidSession(); b.setId(id);

        // Act
        boolean eq = a.equals(b);

        // Assert
        assertThat(eq).isTrue();
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @DisplayName("equals/hashCode: dos sesiones con distinto UUID no deben ser iguales")
    @Test
    void should_ConsiderDifferent_When_DifferentUUID() {
        // Arrange
        SessionEntity a = buildValidSession(); a.setId(UUID.randomUUID());
        SessionEntity b = buildValidSession(); b.setId(UUID.randomUUID());

        // Act
        boolean eq = a.equals(b);

        // Assert
        assertThat(eq).isFalse();
    }

}
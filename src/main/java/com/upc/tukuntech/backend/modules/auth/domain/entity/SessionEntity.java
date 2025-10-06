package com.upc.tukuntech.backend.modules.auth.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(
        name = "sessions",
        indexes = {
                @Index(name = "idx_sessions_user", columnList = "user_id"),
                @Index(name = "idx_sessions_refresh_hash", columnList = "refresh_token_hash")
        }
)
@Getter @Setter
@ToString(exclude = "refreshTokenHash")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class SessionEntity {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    @EqualsAndHashCode.Include
    private UUID id = UUID.randomUUID();

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_sessions_user"))
    @NotNull
    private UserEntity user;

    @Column(name = "refresh_token_hash", nullable = false, length = 64)
    @NotBlank
    @Size(max = 64)
    private String refreshTokenHash;

    @Column(name = "access_expires_at",  nullable = false)
    @NotNull
    private Instant accessExpiresAt;

    @Column(name = "refresh_expires_at", nullable = false)
    @NotNull
    private Instant refreshExpiresAt;

    @Column(name = "is_active", nullable = false)
    @NotNull
    private Boolean active = true;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Column(name = "ip", length = 64)
    @Size(max = 64)
    private String ip;

    @Column(name = "user_agent", length = 255)
    @Size(max = 255)
    private String userAgent;

    @Column(name = "created_at", nullable = false, updatable = false)
    @NotNull
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @NotNull
    private Instant updatedAt = Instant.now();


    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
        if (updatedAt == null) updatedAt = createdAt;
        normalize();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = Instant.now();
        normalize();
    }

    private void normalize() {
        if (ip != null)        ip = ip.trim().toLowerCase(Locale.ROOT);
        if (userAgent != null) userAgent = userAgent.trim();
        if (refreshTokenHash != null) refreshTokenHash = refreshTokenHash.trim();
    }

    /** ¿Expiró el access token? */
    public boolean isAccessExpired(Instant now) {
        return accessExpiresAt != null && !accessExpiresAt.isAfter(now);
    }

    /** ¿Expiró el refresh token? */
    public boolean isRefreshExpired(Instant now) {
        return refreshExpiresAt != null && !refreshExpiresAt.isAfter(now);
    }

    /** ¿Se puede refrescar? (sesión activa y refresh no expirado) */
    public boolean canRefresh(Instant now) {
        return Boolean.TRUE.equals(active) && !isRefreshExpired(now);
    }

    /** Revoca la sesión (idempotente). */
    public void revoke() {
        if (Boolean.TRUE.equals(active)) {
            this.active = false;
            this.revokedAt = Instant.now();
        }
    }
}
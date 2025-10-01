package com.upc.tukuntech.backend.modules.auth.domain.service;

import com.upc.tukuntech.backend.config.JwtProperties;
import com.upc.tukuntech.backend.modules.auth.domain.entity.SessionEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.SessionRepository;
import com.upc.tukuntech.backend.shared.util.CryptoUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SessionService {
    private final SessionRepository sessionRepository;
    private final JwtProperties jwtProperties;

    public SessionService(SessionRepository sessionRepository, JwtProperties jwtProperties) {
        this.sessionRepository = sessionRepository;
        this.jwtProperties = jwtProperties;
    }

    // ✅ Registrar nueva sesión y devolver refresh token en claro
    @Transactional
    public String registerLogin(UserEntity user, String ip, String userAgent, Instant accessExpAt) {
        List<SessionEntity> active = sessionRepository.findByUserAndActiveTrue(user);
        int max = jwtProperties.getMaximumSessions();
        if (active.size() >= max) {
            SessionEntity oldest = active.get(0);
            oldest.setActive(false);
            oldest.setRevokedAt(Instant.now());
            sessionRepository.save(oldest);
        }

        String refreshToken = CryptoUtils.randomToken(32); // token en claro
        String refreshHash = CryptoUtils.sha256Hex(refreshToken); // lo que guardamos en BD
        Instant now = Instant.now();
        Instant refreshExpAt = now.plus(jwtProperties.getRefreshTokenExpiration());

        SessionEntity s = new SessionEntity();
        s.setId(UUID.randomUUID());
        s.setUser(user);
        s.setActive(true);
        s.setCreatedAt(now);
        s.setUpdatedAt(now);
        s.setIp(ip);
        s.setUserAgent(userAgent);
        s.setAccessExpiresAt(accessExpAt);
        s.setRefreshExpiresAt(refreshExpAt);
        s.setRefreshTokenHash(refreshHash);

        sessionRepository.save(s);
        return refreshToken;
    }

    // Validar un refresh token activo
    public Optional<SessionEntity> validateRefreshToken(String refreshToken) {
        String hash = CryptoUtils.sha256Hex(refreshToken);
        return sessionRepository.findByRefreshTokenHashAndActiveTrue(hash);
    }

    // Revocar un refresh token
    public boolean revokeRefreshToken(String refreshToken) {
        String hash = CryptoUtils.sha256Hex(refreshToken);
        return sessionRepository.findByRefreshTokenHashAndActiveTrue(hash)
                .map(session -> {
                    session.setActive(false);
                    session.setRevokedAt(Instant.now());
                    sessionRepository.save(session);
                    return true;
                })
                .orElse(false);
    }

    // Actualizar fecha de expiración del access token
    public void updateAccessExpiry(SessionEntity session, Instant newExpiry) {
        session.setAccessExpiresAt(newExpiry);
        sessionRepository.save(session);
    }
}

package com.upc.tukuntech.backend.modules.auth.application.service;

import com.upc.tukuntech.backend.modules.auth.application.dto.*;
import com.upc.tukuntech.backend.modules.auth.application.mapper.UserMapper;
import com.upc.tukuntech.backend.modules.auth.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.RoleRepository;
import com.upc.tukuntech.backend.modules.auth.domain.repository.UserRepository;
import com.upc.tukuntech.backend.modules.auth.infrastructure.security.JwtService;
import com.upc.tukuntech.backend.modules.auth.domain.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthApplicationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final SessionService sessionService;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public AuthApplicationService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserRepository userRepository,
            SessionService sessionService,
            PasswordEncoder passwordEncoder,
            RoleRepository roleRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    // ---------------- LOGIN ----------------
    public LoginResponse login(LoginRequest request, String clientIp, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        UserEntity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String accessToken = jwtService.generateAccessToken(user);
        long accessTtl     = jwtService.getAccessTtlSeconds();
        Instant accessExpAt = Instant.now().plusSeconds(accessTtl);

        String refreshToken = sessionService.registerLogin(user, clientIp, userAgent, accessExpAt);

        var roles = user.getRoles().stream().map(r -> r.getName()).collect(Collectors.toSet());
        UserSummary summary = new UserSummary(user.getId(), user.getEmail(), roles);

        return new LoginResponse(accessToken, "Bearer", accessTtl, refreshToken, summary);
    }

    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        // Normalizar role a mayúsculas
        String inputRole = request.role().toUpperCase();

        String normalizedRole = switch (inputRole) {
            case "PATIENT" -> "PATIENT";
            case "ATTENDANT" -> "ATTENDANT";
            case "ADMINISTRATOR" -> "ADMIN";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        };

        UserEntity user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        // asignar rol
        RoleEntity role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));
        user.getRoles().add(role);

        UserEntity saved = userRepository.save(user);

        return new RegisterResponse(saved.getId(), saved.getEmail(), "User registered successfully");
    }

    //TEMPORAL
    public List<String> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleEntity::getName)
                .toList();
    }

    public UserProfileResponse getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        return new UserProfileResponse(
                user.getId().toString(),
                user.getFirstName(),
                user.getLastName(),
                user.getDni(),
                user.getAge(),
                user.getGender().name(),
                user.getBloodGroup().name(),
                user.getNationality().name(),
                user.getAllergy().name()
        );
    }

    // ---------------- REFRESH ----------------
    public TokenRefreshResponse refreshAccessToken(String refreshToken, String clientIp, String userAgent) {
        var session = sessionService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        // Validar expiración del refresh token
        if (session.getRefreshExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserEntity user = session.getUser();

        // Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(user);
        long accessTtl = jwtService.getAccessTtlSeconds();
        Instant newAccessExpAt = Instant.now().plusSeconds(accessTtl);

        // Actualizar expiración en la sesión
        sessionService.updateAccessExpiry(session, newAccessExpAt);

        return new TokenRefreshResponse(
                newAccessToken,
                "Bearer",
                accessTtl,
                refreshToken // reusamos el mismo refresh mientras siga activo
        );
    }

    // ---------------- LOGOUT ----------------
    public void logout(String refreshToken) {
        boolean revoked = sessionService.revokeRefreshToken(refreshToken);
        if (!revoked) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

}

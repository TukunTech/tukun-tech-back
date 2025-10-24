package com.upc.tukuntech.backend.modules.iam.application.service;

import com.upc.tukuntech.backend.modules.iam.application.dto.LoginRequest;
import com.upc.tukuntech.backend.modules.iam.application.dto.LoginResponse;
import com.upc.tukuntech.backend.modules.iam.application.dto.TokenRefreshResponse;
import com.upc.tukuntech.backend.modules.iam.application.dto.RegisterRequest;
import com.upc.tukuntech.backend.modules.iam.application.dto.RegisterResponse;
import com.upc.tukuntech.backend.modules.iam.application.dto.UserSummary;
import com.upc.tukuntech.backend.modules.iam.application.mapper.UserMapper;
import com.upc.tukuntech.backend.modules.iam.domain.entity.RoleEntity;
import com.upc.tukuntech.backend.modules.iam.domain.entity.UserIdentity;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.RoleRepository;
import com.upc.tukuntech.backend.modules.iam.domain.repositories.UserRepository;
import com.upc.tukuntech.backend.modules.iam.domain.service.SessionService;
import com.upc.tukuntech.backend.modules.iam.infrastructure.security.JwtService;
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

        UserIdentity user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        String accessToken = jwtService.generateAccessToken(user);
        long accessTtl = jwtService.getAccessTtlSeconds();
        Instant accessExpAt = Instant.now().plusSeconds(accessTtl);

        String refreshToken = sessionService.registerLogin(user, clientIp, userAgent, accessExpAt);

        var roles = user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());
        UserSummary summary = new UserSummary(user.getId(), user.getEmail(), roles);

        return new LoginResponse(accessToken, "Bearer", accessTtl, refreshToken, summary);
    }

    // ---------------- REGISTER ----------------
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        String inputRole = request.role().toUpperCase();

        String normalizedRole = switch (inputRole) {
            case "PATIENT" -> "PATIENT";
            case "ATTENDANT" -> "ATTENDANT";
            case "ADMINISTRATOR" -> "ADMIN";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role");
        };

        UserIdentity user = UserMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        RoleEntity role = roleRepository.findByName(normalizedRole)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role not found"));

        user.getRoles().add(role);
        UserIdentity saved = userRepository.save(user);

        // Aquí solo se crea el usuario base, el perfil lo crea Profiles
        return new RegisterResponse(saved.getId(), saved.getEmail(), "User registered successfully");
    }

    // ---------------- REFRESH ----------------
    public TokenRefreshResponse refreshAccessToken(String refreshToken, String clientIp, String userAgent) {
        var session = sessionService.validateRefreshToken(refreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

        if (session.getRefreshExpiresAt().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        UserIdentity user = session.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);
        long accessTtl = jwtService.getAccessTtlSeconds();
        Instant newAccessExpAt = Instant.now().plusSeconds(accessTtl);

        sessionService.updateAccessExpiry(session, newAccessExpAt);

        return new TokenRefreshResponse(newAccessToken, "Bearer", accessTtl, refreshToken);
    }

    // ---------------- LOGOUT ----------------
    public void logout(String refreshToken) {
        boolean revoked = sessionService.revokeRefreshToken(refreshToken);
        if (!revoked) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
        }
    }

    // ---------------- UTILS ----------------
    public List<String> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(RoleEntity::getName)
                .toList();
    }

    public UserSummary getAuthenticatedIdentity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        UserIdentity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        var roles = user.getRoles().stream().map(RoleEntity::getName).collect(Collectors.toSet());
        return new UserSummary(user.getId(), user.getEmail(), roles);
    }
}

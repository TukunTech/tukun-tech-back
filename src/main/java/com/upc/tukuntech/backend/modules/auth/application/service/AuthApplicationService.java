package com.upc.tukuntech.backend.modules.auth.application.service;

import com.upc.tukuntech.backend.modules.auth.application.dto.*;
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

        UserEntity user = new UserEntity();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setDni(request.dni());

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
}

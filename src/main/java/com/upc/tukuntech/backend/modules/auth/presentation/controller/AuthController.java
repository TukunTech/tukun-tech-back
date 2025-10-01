package com.upc.tukuntech.backend.modules.auth.presentation.controller;

import com.upc.tukuntech.backend.modules.auth.application.dto.*;
import com.upc.tukuntech.backend.modules.auth.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.auth.domain.entity.UserEntity;
import com.upc.tukuntech.backend.modules.auth.domain.repository.UserRepository;
import com.upc.tukuntech.backend.modules.auth.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/auth")
@Tag(
        name = "Auth",
        description = "Controller for authentication and user sessions"
)
public class AuthController {

    private final AuthApplicationService authApp;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthController(AuthApplicationService authApp, JwtService jwtService, UserRepository userRepository) {
        this.authApp = authApp;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate a user with email and password, returning access & refresh tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login, returns JWT tokens"),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password"),
                    @ApiResponse(responseCode = "403", description = "User is disabled or blocked")
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(authApp.login(request, ip, ua));
    }

    @PostMapping("/register")
    @Operation(
            summary = "User registration",
            description = "Register a new user account and return confirmation.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User successfully registered"),
                    @ApiResponse(responseCode = "400", description = "Invalid request data (email already used, weak password, etc.)")
            }
    )
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authApp.register(request));
    }

    private String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return req.getRemoteAddr();
    }

    // TEMPORAL
    @GetMapping("/roles")
    public ResponseEntity<?> listRoles() {
        return ResponseEntity.ok(authApp.getAllRoles());
    }


    @GetMapping("/me")
    @Operation(
            summary = "Get authenticated user info",
            description = "Return the personal profile of the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"), // <<--- 🔑
            responses = {
                    @ApiResponse(responseCode = "200", description = "User info retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized, invalid or expired token")
            }
    )
    public ResponseEntity<UserProfileResponse> getAuthenticatedUser(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        String email;
        try {
            email = jwtService.getEmailFromToken(token);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserProfileResponse response = new UserProfileResponse(
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

        return ResponseEntity.ok(response);
    }




}

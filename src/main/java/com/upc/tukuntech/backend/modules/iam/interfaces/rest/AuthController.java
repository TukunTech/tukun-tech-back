package com.upc.tukuntech.backend.modules.iam.interfaces.rest;

import com.upc.tukuntech.backend.modules.iam.application.dto.LoginRequest;
import com.upc.tukuntech.backend.modules.iam.application.dto.LoginResponse;
import com.upc.tukuntech.backend.modules.iam.application.dto.RefreshRequest;
import com.upc.tukuntech.backend.modules.iam.application.dto.TokenRefreshResponse;
import com.upc.tukuntech.backend.modules.iam.application.dto.RegisterRequest;
import com.upc.tukuntech.backend.modules.iam.application.dto.RegisterResponse;
import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.iam.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Endpoints for authentication, JWT tokens and session management")
public class AuthController {

    private final AuthApplicationService authApp;
    private final JwtService jwtService;

    public AuthController(AuthApplicationService authApp, JwtService jwtService) {
        this.authApp = authApp;
        this.jwtService = jwtService;
    }

    @Operation(summary = "User login",
            description = "Authenticate a user with email and password. Returns access and refresh tokens.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login",
                            content = @Content(schema = @Schema(implementation = LoginResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http) {
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(authApp.login(request, ip, ua));
    }

    @Operation(summary = "Register new user",
            description = "Registers a new user account (IAM context only). After this, a profile can be created in /profiles.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User registered successfully",
                            content = @Content(schema = @Schema(implementation = RegisterResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input or email already exists")
            })
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authApp.register(request));
    }

    @Operation(summary = "List all available roles",
            description = "Returns all roles currently available in the IAM system.")
    @GetMapping("/roles")
    public ResponseEntity<?> listRoles() {
        return ResponseEntity.ok(authApp.getAllRoles());
    }

    @Operation(summary = "Refresh access token",
            description = "Generates a new access token using a valid refresh token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "New access token generated",
                            content = @Content(schema = @Schema(implementation = TokenRefreshResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
            })
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshAccessToken(@RequestBody RefreshRequest request,
                                                                   HttpServletRequest http) {
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(authApp.refreshAccessToken(request.refreshToken(), ip, ua));
    }

    @Operation(summary = "Logout",
            description = "Revokes the given refresh token and terminates the session.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired token")
            })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshRequest request) {
        authApp.logout(request.refreshToken());
        return ResponseEntity.ok().build();
    }

    private String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return req.getRemoteAddr();
    }
}

package com.upc.tukuntech.backend.modules.auth.controller;
import com.upc.tukuntech.backend.modules.auth.dto.LoginRequest;
import com.upc.tukuntech.backend.modules.auth.dto.LoginResponse;
import com.upc.tukuntech.backend.modules.auth.service.AuthApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/auth")
@Tag(
        name = "Auth",
        description = "Controller for authentication and user sessions"
)
public class AuthController {
    private final AuthApplicationService authApp;

    public AuthController(AuthApplicationService authApp) {
        this.authApp = authApp;
    }

    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticate a user with email and password, returning access & refresh tokens.", // 👈 descripción detallada
            responses = {
                    @ApiResponse(responseCode = "200", description = "Successful login, returns JWT tokens"),
                    @ApiResponse(responseCode = "401", description = "Invalid email or password"),
                    @ApiResponse(responseCode = "403", description = "User is disabled or blocked")
            }
    )
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request, HttpServletRequest http){
        String ip = clientIp(http);
        String ua = http.getHeader("User-Agent");
        return ResponseEntity.ok(authApp.login(request, ip, ua));

    }

    private String clientIp(HttpServletRequest req){
        String xf = req.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) return xf.split(",")[0].trim();
        return req.getRemoteAddr();
    }


}
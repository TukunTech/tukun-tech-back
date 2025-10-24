package com.upc.tukuntech.backend.modules.iam.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/iam/test")
@Tag(
        name = "IAM Test",
        description = "Endpoints for verifying IAM authentication and role-based access"
)
@SecurityRequirement(name = "bearerAuth") // 🔑 Para que Swagger exija JWT
public class TestController {

    @GetMapping("/me")
    @Operation(summary = "Get current authentication info", description = "Returns the JWT-authenticated user info.")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(summary = "Ping for ADMINISTRATOR role")
    public String adminPing() {
        return "pong admin ✅";
    }

    @GetMapping("/attendant/ping")
    @PreAuthorize("hasRole('ATTENDANT')")
    @Operation(summary = "Ping for ATTENDANT role")
    public String attendantPing() {
        return "pong attendant ✅";
    }

    @GetMapping("/patient/ping")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Ping for PATIENT role")
    public String patientPing() {
        return "pong patient ✅";
    }
}
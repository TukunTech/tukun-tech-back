package com.upc.tukuntech.backend.modules.auth.presentation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(
        name = "Test",
        description = "Test roles and users"
)
public class TestController {

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication auth) {
        return ResponseEntity.ok(auth);
    }

    @GetMapping("/admin/ping")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public String adminPing() {
        return "pong admin";
    }

    @GetMapping("/attendant/ping")
    @PreAuthorize("hasRole('ATTENDANT')")
    public String attendantPing() {
        return "pong attendant";
    }

    @GetMapping("/patient/ping")
    @PreAuthorize("hasRole('PATIENT')")
    public String patientPing() {
        return "pong patient";
    }
}
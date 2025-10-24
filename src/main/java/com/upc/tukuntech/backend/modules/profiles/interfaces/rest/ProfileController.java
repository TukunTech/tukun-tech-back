package com.upc.tukuntech.backend.modules.profiles.interfaces.rest;

import com.upc.tukuntech.backend.modules.iam.application.dto.UserProfileResponse;
import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.profiles.application.dto.CreateProfileRequest;
import com.upc.tukuntech.backend.modules.profiles.application.service.ProfileApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@Tag(name = "Profiles", description = "Endpoints for managing user profiles (patients, attendants, administrators)")
public class ProfileController {

    private final ProfileApplicationService profileApp;
    private final AuthApplicationService authApp;

    public ProfileController(ProfileApplicationService profileApp, AuthApplicationService authApp) {
        this.profileApp = profileApp;
        this.authApp = authApp;
    }

    @PostMapping
    @Operation(
            summary = "Create user profile",
            description = "Creates a personal profile for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile created successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Profile already exists or invalid input")
            })
    public ResponseEntity<UserProfileResponse> createProfile(@RequestBody @Valid CreateProfileRequest request) {
        var identity = authApp.getAuthenticatedIdentity(); // 🔐 obtiene usuario logeado
        return ResponseEntity.ok(profileApp.createProfile(identity.id(), request));
    }

    @Operation(summary = "Get authenticated user profile",
            description = "Returns the personal profile for the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Invalid or expired JWT"),
                    @ApiResponse(responseCode = "404", description = "Profile not found")
            })
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getAuthenticatedProfile() {
        var identity = authApp.getAuthenticatedIdentity();
        return ResponseEntity.ok(profileApp.getProfileByUserId(identity.id()));
    }
}

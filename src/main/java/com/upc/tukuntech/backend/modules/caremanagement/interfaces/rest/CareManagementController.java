package com.upc.tukuntech.backend.modules.caremanagement.interfaces.rest;

import com.upc.tukuntech.backend.modules.caremanagement.application.service.CareManagementApplicationService;
import com.upc.tukuntech.backend.modules.profiles.application.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "Care Management", description = "Manage caregiver–patient assignments and relationships")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/care")
public class CareManagementController {

    private final CareManagementApplicationService service;

    public CareManagementController(CareManagementApplicationService service) {
        this.service = service;
    }

    // --------------------------------------------------------------
    // 🔹 ASIGNAR PACIENTE
    // --------------------------------------------------------------
    @Operation(
            summary = "Assign a patient to the authenticated caregiver",
            description = """
        Allows an authenticated **caregiver** or **administrator** to assign an existing patient
        to their care list.  
        You must send a JSON body with `"userId"`.
        """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            schema = @Schema(example = "{ \"userId\": 8 }"),
                            examples = {
                                    @ExampleObject(
                                            name = "Assign patient",
                                            value = "{ \"userId\": 8 }"
                                    )
                            }
                    )
            )
    )
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ATTENDANT','ADMINISTRATOR')")
    public ResponseEntity<UserProfileResponse> assignPatient(
            @RequestBody @NotNull Map<String, Long> body
    ) {
        Long userId = body.get("userId");
        return ResponseEntity.ok(service.assignPatientByUserId(userId));
    }

    // --------------------------------------------------------------
    // 🔹 DESASIGNAR PACIENTE
    // --------------------------------------------------------------
    @Operation(
            summary = "Unassign a patient from the authenticated caregiver",
            description = "Removes a caregiver–patient assignment. Only accessible to caregivers or administrators.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Assignment removed successfully."),
                    @ApiResponse(responseCode = "403", description = "Forbidden: not caregiver or admin.", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Assignment not found.", content = @Content)
            }
    )
    @DeleteMapping("/unassign/{userId}")
    @PreAuthorize("hasAnyRole('ATTENDANT','ADMINISTRATOR')")
    public ResponseEntity<Void> unassignPatient(@PathVariable Long userId) {
        service.unassignPatientByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    // --------------------------------------------------------------
    // 🔹 VER PACIENTES ASIGNADOS (CON DATOS COMPLETOS)
    // --------------------------------------------------------------
    @Operation(
            summary = "Get all patients assigned to the authenticated caregiver",
            description = """
            Returns detailed profile information for all patients currently assigned to the
            authenticated caregiver.  
            Only accessible to caregivers or administrators.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of patient profiles.",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden: not caregiver or admin.", content = @Content)
            }
    )
    @GetMapping("/my-patients")
    @PreAuthorize("hasAnyRole('ATTENDANT','ADMINISTRATOR')")
    public ResponseEntity<List<UserProfileResponse>> getMyPatients() {
        return ResponseEntity.ok(service.getMyPatientProfiles());
    }

    // --------------------------------------------------------------
    // 🔹 VER CUIDADORES ASIGNADOS (CON DATOS COMPLETOS)
    // --------------------------------------------------------------
    @Operation(
            summary = "Get all caregivers assigned to the authenticated patient",
            description = """
            Returns detailed profile information for all caregivers currently assigned
            to the authenticated patient.  
            Only accessible to patients or administrators.
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "List of caregiver profiles.",
                            content = @Content(schema = @Schema(implementation = UserProfileResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden: not patient or admin.", content = @Content)
            }
    )
    @GetMapping("/my-caregivers")
    @PreAuthorize("hasAnyRole('PATIENT','ADMINISTRATOR')")
    public ResponseEntity<List<UserProfileResponse>> getMyCaregivers() {
        return ResponseEntity.ok(service.getMyCaregiverProfiles());
    }
}

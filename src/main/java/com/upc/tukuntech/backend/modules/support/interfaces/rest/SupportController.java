package com.upc.tukuntech.backend.modules.support.interfaces.rest;

import com.upc.tukuntech.backend.modules.iam.application.service.AuthApplicationService;
import com.upc.tukuntech.backend.modules.support.application.dto.CreateResponseRequest;
import com.upc.tukuntech.backend.modules.support.application.dto.CreateTicketRequest;
import com.upc.tukuntech.backend.modules.support.application.dto.TicketResponse;
import com.upc.tukuntech.backend.modules.support.application.queries.GetTicketsByUserQuery;
import com.upc.tukuntech.backend.modules.support.application.service.SupportApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/support")
@Tag(name = "Support", description = "Endpoints for creating and managing support tickets")
public class SupportController {

    private final SupportApplicationService supportApp;
    private final AuthApplicationService authApp;

    public SupportController(SupportApplicationService supportApp, AuthApplicationService authApp) {
        this.supportApp = supportApp;
        this.authApp = authApp;
    }

    // ✅ Crear un nuevo ticket (usuarios autenticados o anónimos)
    @PostMapping("/tickets")
    @Operation(
            summary = "Create a new support ticket",
            description = "Allows an authenticated user to create a new support ticket.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ticket created successfully",
                            content = @Content(schema = @Schema(implementation = TicketResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data")
            }
    )
    public ResponseEntity<TicketResponse> createTicket(@RequestBody @Valid CreateTicketRequest request) {
        var identity = authApp.getAuthenticatedIdentity();
        return ResponseEntity.ok(supportApp.createTicket(identity.id(), request));
    }

    // ✅ Añadir una respuesta a un ticket (solo admin)
    @PostMapping("/tickets/{ticketId}/responses")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Add a response to a support ticket",
            description = "Allows administrators or support staff to respond to a user's ticket.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<TicketResponse> addResponse(
            @PathVariable Long ticketId,
            @RequestBody @Valid CreateResponseRequest request
    ) {
        var identity = authApp.getAuthenticatedIdentity();
        return ResponseEntity.ok(supportApp.addResponse(ticketId, identity.id(), request));
    }

    // ✅ Cambiar estado de un ticket (solo admin)
    @PatchMapping("/tickets/{ticketId}/status")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "Update ticket status",
            description = "Allows an administrator to mark a ticket as RESOLVED or CLOSED.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long ticketId,
            @RequestParam String status
    ) {
        return ResponseEntity.ok(supportApp.updateTicketStatus(ticketId, status));
    }

    // ✅ Listar o filtrar tickets (solo admin)
    @GetMapping("/tickets")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @Operation(
            summary = "List or filter support tickets",
            description = "Admins can filter tickets by status, userId, or date range.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<List<TicketResponse>> getTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ResponseEntity.ok(supportApp.getTicketsByFilters(status, userId, fromDate, toDate));
    }

    // ✅ Nuevo: obtener los tickets del usuario autenticado (para vista "My tickets")
    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('PATIENT','ATTENDANT')")
    @Operation(
            summary = "Get authenticated user's tickets",
            description = "Returns all tickets created by the currently authenticated user.",
            security = @SecurityRequirement(name = "bearerAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of user's tickets",
                            content = @Content(schema = @Schema(implementation = TicketResponse.class)))
            }
    )
    public ResponseEntity<List<TicketResponse>> getMyTickets() {
        var identity = authApp.getAuthenticatedIdentity();
        return ResponseEntity.ok(supportApp.getTicketsByUser(identity.id()));
    }

}

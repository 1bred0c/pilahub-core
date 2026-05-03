package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TicketTypeDto;
import fpt.edu.sep490.pilahub.dto.request.tickettype.CreateTicketTypeRequest;
import fpt.edu.sep490.pilahub.dto.request.tickettype.UpdateTicketTypeRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.TicketTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
@Tag(name = "Ticket Type", description = "Ticket type management endpoints")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create ticket type (Admin only)", description = "Create a new ticket type")
    @ApiResponse(responseCode = "201", description = "Ticket type created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketTypeDto>> createTicketType(
            @Valid @RequestBody CreateTicketTypeRequest request) {
        TicketTypeDto ticketType = ticketTypeService.createTicketType(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Ticket type created successfully", ticketType));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get ticket type by ID (Admin only)", description = "Retrieve a specific ticket type by ID")
    @ApiResponse(responseCode = "200", description = "Ticket type retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Ticket type not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketTypeDto>> getTicketTypeById(@PathVariable("id") UUID ticketTypeId) {
        TicketTypeDto ticketType = ticketTypeService.getById(ticketTypeId);
        return ResponseEntity.ok(APIResponse.success("Ticket type retrieved successfully", ticketType));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all ticket types (Admin only)", description = "Retrieve all ticket types")
    @ApiResponse(responseCode = "200", description = "Ticket types retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<TicketTypeDto>>> getAllTicketTypes() {
        List<TicketTypeDto> ticketTypes = ticketTypeService.getAll();
        return ResponseEntity.ok(APIResponse.success("Ticket types retrieved successfully", ticketTypes));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update ticket type (Admin only)", description = "Update an existing ticket type")
    @ApiResponse(responseCode = "200", description = "Ticket type updated successfully")
    @ApiResponse(responseCode = "404", description = "Ticket type not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketTypeDto>> updateTicketType(
            @PathVariable("id") UUID ticketTypeId,
            @Valid @RequestBody UpdateTicketTypeRequest request) {
        TicketTypeDto ticketType = ticketTypeService.updateTicketType(ticketTypeId, request);
        return ResponseEntity.ok(APIResponse.success("Ticket type updated successfully", ticketType));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate ticket type (Admin only)", description = "Activate a ticket type")
    @ApiResponse(responseCode = "200", description = "Ticket type activated successfully")
    @ApiResponse(responseCode = "404", description = "Ticket type not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Void>> activateTicketType(@PathVariable("id") UUID ticketTypeId) {
        ticketTypeService.activateTicketType(ticketTypeId);
        return ResponseEntity.ok(APIResponse.success("Ticket type activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate ticket type (Admin only)", description = "Deactivate a ticket type")
    @ApiResponse(responseCode = "200", description = "Ticket type deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Ticket type not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Void>> deactivateTicketType(@PathVariable("id") UUID ticketTypeId) {
        ticketTypeService.deactivateTicketType(ticketTypeId);
        return ResponseEntity.ok(APIResponse.success("Ticket type deactivated successfully", null));
    }
}

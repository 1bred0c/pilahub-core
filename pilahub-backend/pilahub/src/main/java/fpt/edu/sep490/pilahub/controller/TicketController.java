package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TicketDto;
import fpt.edu.sep490.pilahub.dto.request.ticket.CreateTicketRequest;
import fpt.edu.sep490.pilahub.dto.request.ticket.UpdateTicketRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.enums.TicketStatus;
import fpt.edu.sep490.pilahub.service.TicketService;
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
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket", description = "Ticket management endpoints")
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Create ticket", description = "Create a new ticket for current authenticated user")
    @ApiResponse(responseCode = "201", description = "Ticket created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<APIResponse<TicketDto>> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDto ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Ticket created successfully", ticket));
    }

    @GetMapping("/my-tickets")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get my tickets", description = "Retrieve all tickets created by current authenticated user")
    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<APIResponse<List<TicketDto>>> getMyTickets() {
        List<TicketDto> tickets = ticketService.getMyTickets();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d ticket(s)", tickets.size()),
                tickets));
    }

    @GetMapping("/my-tickets/{ticketId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR', 'ADMIN')")
    @Operation(summary = "Get my ticket by ID", description = "Retrieve one ticket created by current authenticated user")
    @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    public ResponseEntity<APIResponse<TicketDto>> getMyTicketById(@PathVariable UUID ticketId) {
        TicketDto ticket = ticketService.getMyTicketById(ticketId);
        return ResponseEntity.ok(APIResponse.success("Ticket retrieved successfully", ticket));
    }

//    @PutMapping("/my-tickets/{ticketId}")
//    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'VENDOR', 'ADMIN')")
//    @Operation(summary = "Update my ticket", description = "Update a ticket created by current authenticated user (only when status is PENDING)")
//    @ApiResponse(responseCode = "200", description = "Ticket updated successfully")
//    @ApiResponse(responseCode = "400", description = "Invalid request")
//    @ApiResponse(responseCode = "404", description = "Ticket not found")
//    @ApiResponse(responseCode = "403", description = "Forbidden")
//    public ResponseEntity<APIResponse<TicketDto>> updateMyTicket(
//            @PathVariable UUID ticketId,
//            @Valid @RequestBody UpdateTicketRequest request) {
//        TicketDto ticket = ticketService.updateMyTicket(ticketId, request);
//        return ResponseEntity.ok(APIResponse.success("Ticket updated successfully", ticket));
//    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all tickets (Admin only)", description = "Retrieve all tickets")
    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<TicketDto>>> getAllTickets() {
        List<TicketDto> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d ticket(s)", tickets.size()),
                tickets));
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get tickets by status (Admin only)", description = "Retrieve tickets filtered by status")
    @ApiResponse(responseCode = "200", description = "Tickets retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<TicketDto>>> getTicketsByStatus(@RequestParam TicketStatus status) {
        List<TicketDto> tickets = ticketService.getTicketsByStatus(status);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d ticket(s) with status %s", tickets.size(), status),
                tickets));
    }

    @GetMapping("/{ticketId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get ticket by ID (Admin only)", description = "Retrieve one ticket by ID")
    @ApiResponse(responseCode = "200", description = "Ticket retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketDto>> getTicketById(@PathVariable UUID ticketId) {
        TicketDto ticket = ticketService.getTicketById(ticketId);
        return ResponseEntity.ok(APIResponse.success("Ticket retrieved successfully", ticket));
    }

    @PatchMapping("/{ticketId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve ticket (Admin only)", description = "Approve a PENDING ticket")
    @ApiResponse(responseCode = "200", description = "Ticket approved successfully")
    @ApiResponse(responseCode = "400", description = "Ticket is not in PENDING status")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketDto>> approveTicket(@PathVariable UUID ticketId) {
        TicketDto ticket = ticketService.approveTicket(ticketId);
        return ResponseEntity.ok(APIResponse.success("Ticket approved successfully", ticket));
    }

    @PatchMapping("/{ticketId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject ticket (Admin only)", description = "Reject a PENDING ticket")
    @ApiResponse(responseCode = "200", description = "Ticket rejected successfully")
    @ApiResponse(responseCode = "400", description = "Ticket is not in PENDING status")
    @ApiResponse(responseCode = "404", description = "Ticket not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<TicketDto>> rejectTicket(@PathVariable UUID ticketId) {
        TicketDto ticket = ticketService.rejectTicket(ticketId);
        return ResponseEntity.ok(APIResponse.success("Ticket rejected successfully", ticket));
    }
}

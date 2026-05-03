package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CoachDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CoachService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coach", description = "Manage coach profiles")
public class CoachController {

    private final CoachService coachService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Create coach profile", description = "Create a new coach profile for the current account")
    @ApiResponse(responseCode = "201", description = "Coach profile created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CoachDto>> createCoach(@Valid @RequestBody CreateCoachRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        CoachDto coach = coachService.createCoach(accountId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Coach profile created successfully", coach));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coach by ID", description = "Retrieve a coach profile by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    public ResponseEntity<APIResponse<CoachDto>> getCoachById(@PathVariable("id") UUID coachId) {
        CoachDto coach = coachService.getById(coachId);
        return ResponseEntity.ok(APIResponse.success("Coach retrieved successfully", coach));
    }

    @GetMapping
    @Operation(summary = "Get all coaches", description = "Retrieve all coach profiles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachDto>>> getAllCoaches() {
        List<CoachDto> coaches = coachService.getAll();
        return ResponseEntity.ok(APIResponse.success("Coaches retrieved successfully", coaches));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active coaches", description = "Retrieve all active coach profiles")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachDto>>> getAllActiveCoaches() {
        List<CoachDto> coaches = coachService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active coaches retrieved successfully", coaches));
    }

    @GetMapping("/search")
    @Operation(summary = "Search coaches by name", description = "Search for coaches by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachDto>>> searchCoaches(@RequestParam("q") String query) {
        List<CoachDto> coaches = coachService.searchByName(query);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", coaches));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Update coach profile", description = "Update coach profile information")
    @ApiResponse(responseCode = "200", description = "Coach profile updated successfully")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CoachDto>> updateCoach(
            @PathVariable("id") UUID coachId,
            @Valid @RequestBody UpdateCoachRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        
        // Verify that the coach belongs to the current user
        if (!coachId.equals(accountId)) {
            throw new IllegalStateException("You can only update your own profile");
        }

        CoachDto coach = coachService.updateCoach(coachId, request);
        return ResponseEntity.ok(APIResponse.success("Coach profile updated successfully", coach));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate coach", description = "Activate a coach profile")
    @ApiResponse(responseCode = "200", description = "Coach activated successfully")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> activateCoach(@PathVariable("id") UUID coachId) {
        coachService.activateCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Coach activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate coach", description = "Deactivate a coach profile")
    @ApiResponse(responseCode = "200", description = "Coach deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deactivateCoach(@PathVariable("id") UUID coachId) {
        coachService.deactivateCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Coach deactivated successfully", null));
    }

    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update coach price per hour (Admin)", description = "Admin updates the hourly rate for a coach")
    @ApiResponse(responseCode = "200", description = "Price updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid price")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> updatePricePerHour(
            @PathVariable("id") UUID coachId,
            @RequestParam("pricePerHour") BigDecimal pricePerHour) {
        coachService.updatePricePerHour(coachId, pricePerHour);
        return ResponseEntity.ok(APIResponse.success("Coach price per hour updated successfully", null));
    }
}

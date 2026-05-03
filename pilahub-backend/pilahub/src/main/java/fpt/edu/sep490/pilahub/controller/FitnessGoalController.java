package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.FitnessGoalDto;
import fpt.edu.sep490.pilahub.dto.request.CreateFitnessGoalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateFitnessGoalRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.FitnessGoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fitness-goals")
@RequiredArgsConstructor
@Tag(name = "Fitness Goal Management", description = "APIs for managing fitness goals")
@SecurityRequirement(name = "bearerAuth")
public class FitnessGoalController {

    private final FitnessGoalService fitnessGoalService;

    // ─── Admin: Create ────────────────────────────────────────────────────────

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create fitness goal (Admin only)",
            description = "Create a new fitness goal entry.")
    @ApiResponse(responseCode = "201", description = "Fitness goal created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or duplicate code")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<FitnessGoalDto>> create(
            @Valid @RequestBody CreateFitnessGoalRequest request) {
        FitnessGoalDto dto = fitnessGoalService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Fitness goal created successfully", dto));
    }

    // ─── Admin: Paginated list ────────────────────────────────────────────────

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all fitness goals with pagination (Admin only)",
            description = "Retrieve a paginated list of all fitness goals.")
    @ApiResponse(responseCode = "200", description = "Fitness goals retrieved successfully")
    public ResponseEntity<APIResponse<Page<FitnessGoalDto>>> getAll(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Number of items per page", example = "20")
            @RequestParam(defaultValue = "20") int size,

            @Parameter(description = "Sort field", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "Sort direction (asc or desc)", example = "asc")
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<FitnessGoalDto> goals = fitnessGoalService.getAll(pageable);

        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d fitness goal(s)", goals.getTotalElements()),
                goals));
    }

    // ─── Public: Active list ──────────────────────────────────────────────────

    @GetMapping("/active")
    @Operation(summary = "Get all active fitness goals",
            description = "Retrieve all active fitness goals. Available to all authenticated users.")
    @ApiResponse(responseCode = "200", description = "Active fitness goals retrieved successfully")
    public ResponseEntity<APIResponse<List<FitnessGoalDto>>> getAllActive() {
        List<FitnessGoalDto> goals = fitnessGoalService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active fitness goals retrieved successfully", goals));
    }

    // ─── Public: Get by ID ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Get fitness goal by ID",
            description = "Retrieve a specific fitness goal by its ID.")
    @ApiResponse(responseCode = "200", description = "Fitness goal found")
    @ApiResponse(responseCode = "404", description = "Fitness goal not found")
    public ResponseEntity<APIResponse<FitnessGoalDto>> getById(@PathVariable UUID id) {
        FitnessGoalDto dto = fitnessGoalService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Fitness goal retrieved successfully", dto));
    }

    // ─── Public: Search ───────────────────────────────────────────────────────

    @GetMapping("/search")
    @Operation(summary = "Search fitness goals by keyword",
            description = "Search fitness goals by Vietnamese name or English description.")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<APIResponse<List<FitnessGoalDto>>> search(
            @Parameter(description = "Search keyword", example = "đau lưng")
            @RequestParam String keyword) {
        List<FitnessGoalDto> goals = fitnessGoalService.search(keyword);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", goals));
    }

    // ─── Admin: Update ────────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update fitness goal (Admin only)",
            description = "Update an existing fitness goal's details.")
    @ApiResponse(responseCode = "200", description = "Fitness goal updated successfully")
    @ApiResponse(responseCode = "404", description = "Fitness goal not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<FitnessGoalDto>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFitnessGoalRequest request) {
        FitnessGoalDto dto = fitnessGoalService.update(id, request);
        return ResponseEntity.ok(APIResponse.success("Fitness goal updated successfully", dto));
    }

    // ─── Admin: Deactivate ────────────────────────────────────────────────────

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate fitness goal (Admin only)",
            description = "Soft-delete a fitness goal by setting it to inactive.")
    @ApiResponse(responseCode = "200", description = "Fitness goal deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Fitness goal not found")
    public ResponseEntity<APIResponse<Void>> deactivate(@PathVariable UUID id) {
        fitnessGoalService.deactivate(id);
        return ResponseEntity.ok(APIResponse.success("Fitness goal deactivated successfully", null));
    }

    // ─── Admin: Activate ──────────────────────────────────────────────────────

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activate fitness goal (Admin only)",
            description = "Re-activate a previously deactivated fitness goal.")
    @ApiResponse(responseCode = "200", description = "Fitness goal activated successfully")
    @ApiResponse(responseCode = "404", description = "Fitness goal not found")
    public ResponseEntity<APIResponse<Void>> activate(@PathVariable UUID id) {
        fitnessGoalService.activate(id);
        return ResponseEntity.ok(APIResponse.success("Fitness goal activated successfully", null));
    }
}

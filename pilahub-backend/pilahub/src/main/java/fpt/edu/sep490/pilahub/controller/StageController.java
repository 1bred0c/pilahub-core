package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.StageDto;
import fpt.edu.sep490.pilahub.dto.request.CreateStageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateStageRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.StageService;
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
@RequestMapping("/api/stages")
@RequiredArgsConstructor
@Tag(name = "Stage", description = "Stage management endpoints")
public class StageController {

    private final StageService stageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create stage", description = "Create a new stage")
    @ApiResponse(responseCode = "201", description = "Stage created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<StageDto>> createStage(@Valid @RequestBody CreateStageRequest request) {
        StageDto stage = stageService.createStage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Stage created successfully", stage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stage by ID", description = "Retrieve a specific stage by its ID")
    @ApiResponse(responseCode = "200", description = "Stage found")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    public ResponseEntity<APIResponse<StageDto>> getStageById(@PathVariable UUID id) {
        StageDto stage = stageService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Stage retrieved successfully", stage));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Get all stages", description = "Retrieve all stages (active and inactive)")
    @ApiResponse(responseCode = "200", description = "Stages retrieved successfully")
    public ResponseEntity<APIResponse<List<StageDto>>> getAllStages() {
        List<StageDto> stages = stageService.getAll();
        return ResponseEntity.ok(APIResponse.success("Stages retrieved successfully", stages));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active stages", description = "Retrieve all active stages")
    @ApiResponse(responseCode = "200", description = "Active stages retrieved successfully")
    public ResponseEntity<APIResponse<List<StageDto>>> getAllActiveStages() {
        List<StageDto> stages = stageService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active stages retrieved successfully", stages));
    }

    @GetMapping("/search")
    @Operation(summary = "Search stages by name", description = "Search for stages by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<StageDto>>> searchStages(@RequestParam String name) {
        List<StageDto> stages = stageService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", stages));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update stage", description = "Update an existing stage")
    @ApiResponse(responseCode = "200", description = "Stage updated successfully")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<StageDto>> updateStage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStageRequest request) {
        StageDto stage = stageService.updateStage(id, request);
        return ResponseEntity.ok(APIResponse.success("Stage updated successfully", stage));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Deactivate stage", description = "Mark a stage as inactive")
    @ApiResponse(responseCode = "200", description = "Stage deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    public ResponseEntity<APIResponse<Void>> deactivateStage(@PathVariable UUID id) {
        stageService.deactivateStage(id);
        return ResponseEntity.ok(APIResponse.success("Stage deactivated successfully", null));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Activate stage", description = "Mark a stage as active")
    @ApiResponse(responseCode = "200", description = "Stage activated successfully")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    public ResponseEntity<APIResponse<Void>> activateStage(@PathVariable UUID id) {
        stageService.activateStage(id);
        return ResponseEntity.ok(APIResponse.success("Stage activated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete stage", description = "Permanently delete a stage")
    @ApiResponse(responseCode = "200", description = "Stage deleted successfully")
    @ApiResponse(responseCode = "404", description = "Stage not found")
    public ResponseEntity<APIResponse<Void>> deleteStage(@PathVariable UUID id) {
        stageService.deleteStage(id);
        return ResponseEntity.ok(APIResponse.success("Stage deleted successfully", null));
    }
}

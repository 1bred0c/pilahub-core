package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PersonalStageDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PersonalStageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal-stages")
@RequiredArgsConstructor
@Tag(name = "Personal Stage", description = "Personal stage management endpoints")
public class PersonalStageController {

    private final PersonalStageService personalStageService;

    @PostMapping
    @Operation(summary = "Create personal stage", description = "Create a new personal stage for a roadmap")
    @ApiResponse(responseCode = "201", description = "Personal stage created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<PersonalStageDto>> createPersonalStage(@Valid @RequestBody CreatePersonalStageRequest request) {
        PersonalStageDto stage = personalStageService.createStage(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal stage created successfully", stage));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get personal stage by ID", description = "Retrieve a specific personal stage by its ID")
    @ApiResponse(responseCode = "200", description = "Personal stage found")
    @ApiResponse(responseCode = "404", description = "Personal stage not found")
    public ResponseEntity<APIResponse<PersonalStageDto>> getPersonalStageById(@PathVariable UUID id) {
        PersonalStageDto stage = personalStageService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Personal stage retrieved successfully", stage));
    }

    @GetMapping("/roadmap/{roadmapId}")
    @Operation(summary = "Get stages by roadmap", description = "Retrieve all personal stages for a specific roadmap, ordered by stage order")
    @ApiResponse(responseCode = "200", description = "Personal stages retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<List<PersonalStageDto>>> getStagesByRoadmap(@PathVariable UUID roadmapId) {
        List<PersonalStageDto> stages = personalStageService.getByRoadmapId(roadmapId);
        return ResponseEntity.ok(APIResponse.success("Personal stages retrieved successfully", stages));
    }

//    @GetMapping("/completed")
//    @Operation(summary = "Get completed stages", description = "Retrieve all completed personal stages")
//    @ApiResponse(responseCode = "200", description = "Completed stages retrieved successfully")
//    public ResponseEntity<APIResponse<List<PersonalStageDto>>> getCompletedStages() {
//        List<PersonalStageDto> stages = personalStageService.getCompleted();
//        return ResponseEntity.ok(APIResponse.success("Completed stages retrieved successfully", stages));
//    }
//
//    @GetMapping("/incomplete")
//    @Operation(summary = "Get incomplete stages", description = "Retrieve all incomplete personal stages")
//    @ApiResponse(responseCode = "200", description = "Incomplete stages retrieved successfully")
//    public ResponseEntity<APIResponse<List<PersonalStageDto>>> getIncompleteStages() {
//        List<PersonalStageDto> stages = personalStageService.getIncomplete();
//        return ResponseEntity.ok(APIResponse.success("Incomplete stages retrieved successfully", stages));
//    }

    @PutMapping("/{id}")
    @Operation(summary = "Update personal stage", description = "Update an existing personal stage")
    @ApiResponse(responseCode = "200", description = "Personal stage updated successfully")
    @ApiResponse(responseCode = "404", description = "Personal stage not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<PersonalStageDto>> updatePersonalStage(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonalStageRequest request) {
        PersonalStageDto stage = personalStageService.updateStage(id, request);
        return ResponseEntity.ok(APIResponse.success("Personal stage updated successfully", stage));
    }

//    @PatchMapping("/{id}/complete")
//    @Operation(summary = "Mark stage as completed", description = "Mark a personal stage as completed")
//    @ApiResponse(responseCode = "200", description = "Stage marked as completed")
//    @ApiResponse(responseCode = "404", description = "Personal stage not found")
//    public ResponseEntity<APIResponse<PersonalStageDto>> markAsCompleted(@PathVariable UUID id) {
//        PersonalStageDto stage = personalStageService.markAsCompleted(id);
//        return ResponseEntity.ok(APIResponse.success("Stage marked as completed", stage));
//    }
//
//    @PatchMapping("/{id}/incomplete")
//    @Operation(summary = "Mark stage as incomplete", description = "Mark a personal stage as incomplete")
//    @ApiResponse(responseCode = "200", description = "Stage marked as incomplete")
//    @ApiResponse(responseCode = "404", description = "Personal stage not found")
//    public ResponseEntity<APIResponse<PersonalStageDto>> markAsIncomplete(@PathVariable UUID id) {
//        PersonalStageDto stage = personalStageService.markAsIncomplete(id);
//        return ResponseEntity.ok(APIResponse.success("Stage marked as incomplete", stage));
//    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete personal stage", description = "Permanently delete a personal stage")
    @ApiResponse(responseCode = "200", description = "Personal stage deleted successfully")
    @ApiResponse(responseCode = "404", description = "Personal stage not found")
    public ResponseEntity<APIResponse<Void>> deletePersonalStage(@PathVariable UUID id) {
        personalStageService.deleteStage(id);
        return ResponseEntity.ok(APIResponse.success("Personal stage deleted successfully", null));
    }
}

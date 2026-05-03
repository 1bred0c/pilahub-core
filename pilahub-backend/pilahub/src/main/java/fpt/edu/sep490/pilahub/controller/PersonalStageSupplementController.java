package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PersonalStageSupplementDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PersonalStageSupplementService;
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
@RequestMapping("/api/personal-stage-supplements")
@RequiredArgsConstructor
@Tag(name = "Personal Stage Supplement", description = "Personal stage supplement management endpoints")
public class PersonalStageSupplementController {

    private final PersonalStageSupplementService personalStageSupplementService;

    @PostMapping
    @Operation(summary = "Create personal stage supplement", description = "Assign a supplement to a personal stage")
    @ApiResponse(responseCode = "201", description = "Personal stage supplement created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Personal stage or supplement not found")
    public ResponseEntity<APIResponse<PersonalStageSupplementDto>> createPersonalStageSupplement(
            @Valid @RequestBody CreatePersonalStageSupplementRequest request) {
        PersonalStageSupplementDto personalStageSupplement = personalStageSupplementService.createPersonalStageSupplement(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal stage supplement created successfully", personalStageSupplement));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get personal stage supplement by ID", description = "Retrieve a specific personal stage supplement by its ID")
    @ApiResponse(responseCode = "200", description = "Personal stage supplement found")
    @ApiResponse(responseCode = "404", description = "Personal stage supplement not found")
    public ResponseEntity<APIResponse<PersonalStageSupplementDto>> getPersonalStageSupplementById(@PathVariable UUID id) {
        PersonalStageSupplementDto personalStageSupplement = personalStageSupplementService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Personal stage supplement retrieved successfully", personalStageSupplement));
    }

    @GetMapping("/personal-stage/{personalStageId}")
    @Operation(summary = "Get supplements by personal stage ID", description = "Retrieve all supplements for a personal stage")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalStageSupplementDto>>> getSupplementsByPersonalStageId(
            @PathVariable UUID personalStageId) {
        List<PersonalStageSupplementDto> supplements = personalStageSupplementService.getByPersonalStageId(personalStageId);
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @GetMapping("/roadmap/{roadmapId}")
    @Operation(summary = "Get supplements by roadmap ID", description = "Retrieve all supplements for all stages in a roadmap")
    @ApiResponse(responseCode = "200", description = "Supplements retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalStageSupplementDto>>> getSupplementsByRoadmapId(
            @PathVariable UUID roadmapId) {
        List<PersonalStageSupplementDto> supplements = personalStageSupplementService.getByRoadmapId(roadmapId);
        return ResponseEntity.ok(APIResponse.success("Supplements retrieved successfully", supplements));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update personal stage supplement", description = "Update an existing personal stage supplement")
    @ApiResponse(responseCode = "200", description = "Personal stage supplement updated successfully")
    @ApiResponse(responseCode = "404", description = "Personal stage supplement not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<PersonalStageSupplementDto>> updatePersonalStageSupplement(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonalStageSupplementRequest request) {
        PersonalStageSupplementDto personalStageSupplement = personalStageSupplementService.updatePersonalStageSupplement(id, request);
        return ResponseEntity.ok(APIResponse.success("Personal stage supplement updated successfully", personalStageSupplement));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete personal stage supplement", description = "Remove a supplement from a personal stage")
    @ApiResponse(responseCode = "200", description = "Personal stage supplement deleted successfully")
    @ApiResponse(responseCode = "404", description = "Personal stage supplement not found")
    public ResponseEntity<APIResponse<Void>> deletePersonalStageSupplement(@PathVariable UUID id) {
        personalStageSupplementService.deletePersonalStageSupplement(id);
        return ResponseEntity.ok(APIResponse.success("Personal stage supplement deleted successfully", null));
    }

    @DeleteMapping("/personal-stage/{personalStageId}")
    @Operation(summary = "Delete all supplements from personal stage", description = "Remove all supplements from a personal stage")
    @ApiResponse(responseCode = "200", description = "Supplements deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteSupplementsByPersonalStageId(@PathVariable UUID personalStageId) {
        personalStageSupplementService.deleteByPersonalStageId(personalStageId);
        return ResponseEntity.ok(APIResponse.success("Supplements deleted successfully", null));
    }
}

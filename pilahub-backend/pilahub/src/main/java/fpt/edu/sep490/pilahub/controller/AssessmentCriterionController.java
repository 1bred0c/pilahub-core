package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.AssessmentCriterionDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.CreateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.dto.request.assessment.UpdateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.AssessmentCriterionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/assessment-criteria")
@RequiredArgsConstructor
@Tag(name = "Assessment Criterion", description = "Assessment criterion management endpoints")
public class AssessmentCriterionController {

    private final AssessmentCriterionService criterionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create criterion", description = "Create a new assessment criterion")
    @ApiResponse(responseCode = "201", description = "Criterion created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<AssessmentCriterionDto>> create(@Valid @RequestBody CreateAssessmentCriterionRequest request) {
        AssessmentCriterionDto dto = criterionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Assessment criterion created successfully", dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get criterion by ID", description = "Get an assessment criterion by ID")
    @ApiResponse(responseCode = "200", description = "Criterion retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Criterion not found")
    public ResponseEntity<APIResponse<AssessmentCriterionDto>> getById(@PathVariable("id") UUID criterionId) {
        AssessmentCriterionDto dto = criterionService.getById(criterionId);
        return ResponseEntity.ok(APIResponse.success("Assessment criterion retrieved successfully", dto));
    }

    @GetMapping
    @Operation(summary = "Get all criteria", description = "Get all assessment criteria")
    @ApiResponse(responseCode = "200", description = "Criteria retrieved successfully")
    public ResponseEntity<APIResponse<List<AssessmentCriterionDto>>> getAll() {
        List<AssessmentCriterionDto> dtos = criterionService.getAll();
        return ResponseEntity.ok(APIResponse.success("Assessment criteria retrieved successfully", dtos));
    }

    @GetMapping("/active")
    @Operation(summary = "Get active criteria", description = "Get active assessment criteria for assessment forms")
    @ApiResponse(responseCode = "200", description = "Active criteria retrieved successfully")
    public ResponseEntity<APIResponse<List<AssessmentCriterionDto>>> getAllActive() {
        List<AssessmentCriterionDto> dtos = criterionService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active assessment criteria retrieved successfully", dtos));
    }

    @GetMapping("/search")
    @Operation(summary = "Search criteria", description = "Search assessment criteria by name")
    @ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<APIResponse<List<AssessmentCriterionDto>>> search(@RequestParam("name") String name) {
        List<AssessmentCriterionDto> dtos = criterionService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed successfully", dtos));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update criterion", description = "Update an assessment criterion")
    @ApiResponse(responseCode = "200", description = "Criterion updated successfully")
    @ApiResponse(responseCode = "404", description = "Criterion not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<AssessmentCriterionDto>> update(
            @PathVariable("id") UUID criterionId,
            @Valid @RequestBody UpdateAssessmentCriterionRequest request) {
        AssessmentCriterionDto dto = criterionService.update(criterionId, request);
        return ResponseEntity.ok(APIResponse.success("Assessment criterion updated successfully", dto));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate criterion", description = "Soft delete criterion by setting isActive=false")
    @ApiResponse(responseCode = "200", description = "Criterion deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Criterion not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deactivate(@PathVariable("id") UUID criterionId) {
        criterionService.deactivate(criterionId);
        return ResponseEntity.ok(APIResponse.success("Assessment criterion deactivated successfully", null));
    }
}



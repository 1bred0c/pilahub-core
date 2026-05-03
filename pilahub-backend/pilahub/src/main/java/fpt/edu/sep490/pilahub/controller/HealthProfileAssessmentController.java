package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.HealthProfileAssessmentService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/health-profile-assessments")
@RequiredArgsConstructor
@Tag(name = "Health Profile Assessment", description = "APIs for viewing health profile assessments (Read-only for trainees)")
public class HealthProfileAssessmentController {

    private final HealthProfileAssessmentService assessmentService;
    private final SecurityUtil securityUtil;

    @GetMapping("/my-profiles/{healthProfileId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get assessment for a health profile (Trainee only)",
            description = "Retrieve AI-generated assessment for a specific health profile. Trainee can only view their own assessments."
    )
    @ApiResponse(responseCode = "200", description = "Assessment retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Access denied - Assessment does not belong to trainee")
    @ApiResponse(responseCode = "404", description = "Assessment or health profile not found")
    public ResponseEntity<APIResponse<HealthProfileAssessmentDto>> getMyAssessment(
            @PathVariable UUID healthProfileId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileAssessmentDto assessment = assessmentService.getAssessmentByHealthProfileId(healthProfileId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Assessment retrieved successfully", assessment));
    }
}

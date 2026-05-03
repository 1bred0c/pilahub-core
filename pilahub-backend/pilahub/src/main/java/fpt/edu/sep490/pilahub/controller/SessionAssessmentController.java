package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SessionAssessmentDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.SubmitSessionAssessmentRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.SessionAssessmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/live-sessions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Session Assessment", description = "Assessment form endpoints for live sessions")
public class SessionAssessmentController {

    private final SessionAssessmentService sessionAssessmentService;

    @PostMapping("/{liveSessionId}/assessment")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Submit session assessment", description = "Coach submits assessment for completed session")
    @ApiResponse(responseCode = "201", description = "Assessment submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request - session not completed, already assessed, or invalid scores")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only the coach of this session can submit assessment")
    @ApiResponse(responseCode = "404", description = "Session not found")
    public ResponseEntity<APIResponse<SessionAssessmentDto>> submitAssessment(
            @PathVariable UUID liveSessionId,
            @Valid @RequestBody SubmitSessionAssessmentRequest request) {
        SessionAssessmentDto dto = sessionAssessmentService.submitAssessment(liveSessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Session assessment submitted successfully", dto));
    }

    @GetMapping("/{liveSessionId}/assessment")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get assessment by session", description = "Get session assessment detail")
    @ApiResponse(responseCode = "200", description = "Assessment retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Not allowed to access this assessment")
    @ApiResponse(responseCode = "404", description = "Assessment not found")
    public ResponseEntity<APIResponse<SessionAssessmentDto>> getSessionAssessment(@PathVariable UUID liveSessionId) {
        SessionAssessmentDto dto = sessionAssessmentService.getSessionAssessment(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Session assessment retrieved successfully", dto));
    }

    @GetMapping("/my-assessment-history")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my assessment history", description = "Trainee gets own assessment history for charting")
    @ApiResponse(responseCode = "200", description = "Assessment history retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<SessionAssessmentDto>>> getMyAssessmentHistory() {
        List<SessionAssessmentDto> dtos = sessionAssessmentService.getMyAssessmentHistory();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d assessment form(s)", dtos.size()),
                dtos
        ));
    }
}



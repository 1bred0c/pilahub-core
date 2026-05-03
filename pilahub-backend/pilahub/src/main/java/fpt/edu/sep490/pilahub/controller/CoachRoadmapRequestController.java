package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CoachRoadmapRequestDto;
import fpt.edu.sep490.pilahub.dto.request.RejectCoachRoadmapRequestRequest;
import fpt.edu.sep490.pilahub.dto.request.SendRoadmapRequestToCoachRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CoachRoadmapRequestService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coach-roadmap-requests")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coach Roadmap Request", description = "Trainee–coach roadmap creation workflow")
public class CoachRoadmapRequestController {

        private final CoachRoadmapRequestService coachRoadmapRequestService;
        private final SecurityUtil securityUtil;

        // ──────────────────────────────────────────────────────────────────────────
        // Trainee endpoints
        // ──────────────────────────────────────────────────────────────────────────

        @PostMapping
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Send roadmap request to a coach", description = "Trainee selects a coach and sends the essential information needed for the coach "
                        +
                        "to generate an AI-powered roadmap. Use GET /api/coaches/active to browse available coaches.")
        @ApiResponse(responseCode = "201", description = "Request sent successfully")
        @ApiResponse(responseCode = "400", description = "Invalid input or duplicate active request")
        @ApiResponse(responseCode = "404", description = "Coach or fitness goal not found")
        public ResponseEntity<APIResponse<CoachRoadmapRequestDto>> sendRequestToCoach(
                        @Valid @RequestBody SendRoadmapRequestToCoachRequest request) {
                UUID traineeId = securityUtil.getCurrentUserId();
                CoachRoadmapRequestDto result = coachRoadmapRequestService.sendRequestToCoach(traineeId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(APIResponse.success("Roadmap request sent successfully", result));
        }

        @GetMapping("/my-sent")
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Get my sent requests", description = "Returns all roadmap requests the current trainee has sent, ordered by newest first.")
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
        public ResponseEntity<APIResponse<List<CoachRoadmapRequestDto>>> getMySentRequests() {
                UUID traineeId = securityUtil.getCurrentUserId();
                List<CoachRoadmapRequestDto> results = coachRoadmapRequestService.getMySentRequests(traineeId);
                return ResponseEntity.ok(APIResponse.success("Sent requests retrieved successfully", results));
        }

        @DeleteMapping("/{id}/cancel")
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Cancel a pending request", description = "Trainee cancels a request that is still in PENDING status.")
        @ApiResponse(responseCode = "204", description = "Request cancelled successfully")
        @ApiResponse(responseCode = "400", description = "Request is not in PENDING status")
        @ApiResponse(responseCode = "403", description = "Request does not belong to the current trainee")
        @ApiResponse(responseCode = "404", description = "Request not found")
        public ResponseEntity<APIResponse<Void>> cancelRequest(@PathVariable("id") UUID requestId) {
                UUID traineeId = securityUtil.getCurrentUserId();
                coachRoadmapRequestService.cancelRequest(traineeId, requestId);
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body(APIResponse.success("Request cancelled successfully", null));
        }

        // ──────────────────────────────────────────────────────────────────────────
        // Coach endpoints
        // ──────────────────────────────────────────────────────────────────────────

        @GetMapping("/my-received")
        @PreAuthorize("hasRole('COACH')")
        @Operation(summary = "Get all received requests", description = "Returns all roadmap requests the current coach has received, ordered by newest first.")
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
        public ResponseEntity<APIResponse<List<CoachRoadmapRequestDto>>> getMyReceivedRequests() {
                UUID coachId = securityUtil.getCurrentUserId();
                List<CoachRoadmapRequestDto> results = coachRoadmapRequestService.getMyReceivedRequests(coachId);
                return ResponseEntity.ok(APIResponse.success("Received requests retrieved successfully", results));
        }

        @GetMapping("/my-received/pending")
        @PreAuthorize("hasRole('COACH')")
        @Operation(summary = "Get pending received requests", description = "Returns only PENDING roadmap requests the current coach has received.")
        @ApiResponse(responseCode = "200", description = "Requests retrieved successfully")
        public ResponseEntity<APIResponse<List<CoachRoadmapRequestDto>>> getMyPendingReceivedRequests() {
                UUID coachId = securityUtil.getCurrentUserId();
                List<CoachRoadmapRequestDto> results = coachRoadmapRequestService.getMyPendingReceivedRequests(coachId);
                return ResponseEntity.ok(APIResponse.success("Pending requests retrieved successfully", results));
        }

        @PatchMapping("/{id}/accept")
        @PreAuthorize("hasRole('COACH')")
        @Operation(summary = "Accept a roadmap request", description = "Coach accepts a PENDING request. The response contains all parameters "
                        +
                        "(traineeId, primaryGoalId, secondaryGoalIds, workoutLevel, trainingDays, trainingDaySchedules, durationWeeks) "
                        +
                        "needed to call POST /api/roadmaps/ai-generate.")
        @ApiResponse(responseCode = "200", description = "Request accepted successfully")
        @ApiResponse(responseCode = "400", description = "Request is not in PENDING status")
        @ApiResponse(responseCode = "403", description = "Request is not directed to the current coach")
        @ApiResponse(responseCode = "404", description = "Request not found")
        public ResponseEntity<APIResponse<CoachRoadmapRequestDto>> acceptRequest(
                        @PathVariable("id") UUID requestId) {
                UUID coachId = securityUtil.getCurrentUserId();
                CoachRoadmapRequestDto result = coachRoadmapRequestService.acceptRequest(coachId, requestId);
                return ResponseEntity.ok(APIResponse.success("Request accepted successfully", result));
        }

        @PatchMapping("/{id}/reject")
        @PreAuthorize("hasRole('COACH')")
        @Operation(summary = "Reject a roadmap request", description = "Coach rejects a PENDING request with an optional note explaining the reason.")
        @ApiResponse(responseCode = "204", description = "Request rejected successfully")
        @ApiResponse(responseCode = "400", description = "Request is not in PENDING status")
        @ApiResponse(responseCode = "403", description = "Request is not directed to the current coach")
        @ApiResponse(responseCode = "404", description = "Request not found")
        public ResponseEntity<APIResponse<Void>> rejectRequest(
                        @PathVariable("id") UUID requestId,
                        @Valid @RequestBody(required = false) RejectCoachRoadmapRequestRequest rejectRequest) {
                UUID coachId = securityUtil.getCurrentUserId();
                coachRoadmapRequestService.rejectRequest(coachId, requestId, rejectRequest);
                return ResponseEntity.status(HttpStatus.NO_CONTENT)
                                .body(APIResponse.success("Request rejected successfully", null));
        }
}

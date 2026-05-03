package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.RoadmapDto;
import fpt.edu.sep490.pilahub.dto.request.AcceptAIRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.request.CreateRoadmapWithAIRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapWithDetailsRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateFinalHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateProgressRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.RoadmapAIResponse;
import fpt.edu.sep490.pilahub.dto.response.RoadmapWithDetailsResponse;
import fpt.edu.sep490.pilahub.service.RoadmapService;
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
@RequestMapping("/api/roadmaps")
@RequiredArgsConstructor
@Tag(name = "Roadmap", description = "Roadmap management endpoints")
public class RoadmapController {

    private final RoadmapService roadmapService;

    @PostMapping
    @Operation(summary = "Create roadmap", description = "Create a new roadmap")
    @ApiResponse(responseCode = "201", description = "Roadmap created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<RoadmapDto>> createRoadmap(@Valid @RequestBody CreateRoadmapRequest request) {
        RoadmapDto roadmap = roadmapService.createRoadmap(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Roadmap created successfully", roadmap));
    }

     @PostMapping("/with-details")
     @Operation(summary = "Create roadmap with details", description = "Create a new roadmap with all its stages, schedules, and exercises in one transaction")
     @ApiResponse(responseCode = "201", description = "Roadmap with details created successfully")
     @ApiResponse(responseCode = "400", description = "Invalid input")
     @ApiResponse(responseCode = "404", description = "Exercise not found")
     public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> createRoadmapWithDetails(
             @Valid @RequestBody CreateRoadmapWithDetailsRequest request) {
        RoadmapWithDetailsResponse roadmap = roadmapService.createRoadmapWithDetails(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Roadmap with details created successfully", roadmap));
     }

    @PostMapping("/ai-generate")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Generate roadmap with AI (preview only)", description = "Generate a personalized workout roadmap using AI based on trainee's profile, goals, and preferences. Returns AI suggestion without saving. For trainees: traineeId is optional (defaults to self). For coaches: traineeId is required. This process may take up to 2 minutes.")
    @ApiResponse(responseCode = "200", description = "AI roadmap suggestion generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    @ApiResponse(responseCode = "500", description = "AI service unavailable")
    public ResponseEntity<APIResponse<RoadmapAIResponse>> createRoadmapWithAI(
            @Valid @RequestBody CreateRoadmapWithAIRequest request) {
        RoadmapAIResponse aiResponse = roadmapService.createRoadmapWithAI(request);
        return ResponseEntity.ok(APIResponse.success("AI roadmap suggestion generated successfully", aiResponse));
    }

    @PostMapping("/ai-generated/accept")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH')")
    @Operation(summary = "Accept and save AI-generated roadmap", description = "Accept an AI-generated roadmap suggestion and save it to the database. Trainees: traineeId is optional (defaults to self), status is IN_PROGRESS. Coaches: traineeId is required, status is PENDING (requires trainee approval).")
    @ApiResponse(responseCode = "201", description = "AI-generated roadmap saved successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or coach did not specify traineeId")
    @ApiResponse(responseCode = "403", description = "Only trainees and coaches can save roadmaps")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> acceptAIGeneratedRoadmap(
            @Valid @RequestBody AcceptAIRoadmapRequest request) {
        RoadmapWithDetailsResponse roadmap = roadmapService.acceptAIGeneratedRoadmap(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("AI-generated roadmap saved successfully", roadmap));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get roadmap by ID", description = "Retrieve a specific roadmap by its ID")
    @ApiResponse(responseCode = "200", description = "Roadmap found")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<RoadmapDto>> getRoadmapById(@PathVariable UUID id) {
        RoadmapDto roadmap = roadmapService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Roadmap retrieved successfully", roadmap));
    }

    @GetMapping("/{id}/with-details")
    @Operation(summary = "Get roadmap with details", description = "Retrieve a roadmap with all its stages, schedules, and exercises")
    @ApiResponse(responseCode = "200", description = "Roadmap with details found")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> getRoadmapWithDetails(@PathVariable UUID id) {
        RoadmapWithDetailsResponse roadmap = roadmapService.getRoadmapWithDetails(id);
        return ResponseEntity.ok(APIResponse.success("Roadmap with details retrieved successfully", roadmap));
    }

    // @GetMapping("/active")
    // @Operation(summary = "Get all active roadmaps", description = "Retrieve all
    // active roadmaps")
    // @ApiResponse(responseCode = "200", description = "Roadmaps retrieved
    // successfully")
    // public ResponseEntity<APIResponse<List<RoadmapDto>>> getAllActiveRoadmaps() {
    // List<RoadmapDto> roadmaps = roadmapService.getAllActive();
    // return ResponseEntity.ok(APIResponse.success("Roadmaps retrieved
    // successfully", roadmaps));
    // }

    @GetMapping("/search")
    @Operation(summary = "Search roadmaps by title", description = "Search for roadmaps by title (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<RoadmapDto>>> searchRoadmaps(@RequestParam String title) {
        List<RoadmapDto> roadmaps = roadmapService.searchByTitle(title);
        return ResponseEntity.ok(APIResponse.success("Search completed", roadmaps));
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH')")
    @Operation(summary = "Get my roadmaps", description = "Get all roadmaps for the current user. For trainees: returns their own roadmaps. For coaches: returns roadmaps they created for trainees.")
    @ApiResponse(responseCode = "200", description = "Roadmaps retrieved successfully")
    public ResponseEntity<APIResponse<org.springframework.data.domain.Page<RoadmapDto>>> getMyRoadmaps(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) fpt.edu.sep490.pilahub.enums.RoadmapStatus status,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) java.time.Instant startDateFrom,
            @RequestParam(required = false) java.time.Instant startDateTo,
            @RequestParam(required = false) java.time.Instant endDateFrom,
            @RequestParam(required = false) java.time.Instant endDateTo,
            @org.springframework.data.web.PageableDefault(size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) org.springframework.data.domain.Pageable pageable) {

        fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapFilterRequest filter = new fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapFilterRequest(
                title, status, source, startDateFrom, startDateTo, endDateFrom, endDateTo);

        org.springframework.data.domain.Page<RoadmapDto> roadmaps = roadmapService.getMyRoadmaps(filter, pageable);
        return ResponseEntity.ok(APIResponse.success("Roadmaps retrieved successfully", roadmaps));
    }

    @GetMapping("/newest")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get newest roadmap for trainee", description = "Get the most recently created roadmap with full details for the current trainee")
    @ApiResponse(responseCode = "200", description = "Newest roadmap retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No roadmap found for trainee")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> getNewestRoadmapForTrainee() {
        RoadmapWithDetailsResponse roadmap = roadmapService.getNewestRoadmapForTrainee();
        return ResponseEntity.ok(APIResponse.success("Newest roadmap retrieved successfully", roadmap));
    }

    @GetMapping("/my-pending")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get pending roadmap for trainee", description = "Get the roadmap that coach created with full details for the current trainee")
    @ApiResponse(responseCode = "200", description = "Pending roadmap retrieved successfully")
    @ApiResponse(responseCode = "404", description = "No roadmap found for trainee")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> getPendingRoadmapForTrainee() {
        RoadmapWithDetailsResponse roadmap = roadmapService.getPendingRoadmapForTrainee();
        return ResponseEntity.ok(APIResponse.success("Newest roadmap retrieved successfully", roadmap));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update roadmap", description = "Update an existing roadmap")
    @ApiResponse(responseCode = "200", description = "Roadmap updated successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<RoadmapDto>> updateRoadmap(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoadmapRequest request) {
        RoadmapDto roadmap = roadmapService.updateRoadmap(id, request);
        return ResponseEntity.ok(APIResponse.success("Roadmap updated successfully", roadmap));
    }

    @PutMapping("/{id}/reset-progress-and-reschedule")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Reset roadmap progress and reschedule", description = "Reset progress fields for roadmap, stages, schedules, and exercises, then regenerate schedule dates from the new start date and training days")
    @ApiResponse(responseCode = "200", description = "Roadmap progress reset and rescheduled successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> resetProgressAndReschedule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoadmapScheduleRequest request) {
        RoadmapWithDetailsResponse roadmap = roadmapService.resetProgressAndReschedule(id, request);
        return ResponseEntity.ok(APIResponse.success("Roadmap progress reset and rescheduled successfully", roadmap));
    }

    @PutMapping("/{id}/reschedule-incomplete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Reschedule incomplete schedules", description = "Regenerate schedule dates only for schedules where completed is false")
    @ApiResponse(responseCode = "200", description = "Incomplete schedules rescheduled successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<RoadmapWithDetailsResponse>> rescheduleIncomplete(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoadmapScheduleRequest request) {
        RoadmapWithDetailsResponse roadmap = roadmapService.rescheduleIncompleteSchedules(id, request);
        return ResponseEntity.ok(APIResponse.success("Incomplete schedules rescheduled successfully", roadmap));
    }

    @PatchMapping("/{id}/progress")
    @Operation(summary = "Update roadmap progress", description = "Update the progress percentage of a roadmap")
    @ApiResponse(responseCode = "200", description = "Progress updated successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Invalid progress value")
    public ResponseEntity<APIResponse<RoadmapDto>> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProgressRequest request) {
        RoadmapDto roadmap = roadmapService.updateProgress(id, request.progressPercent());
        return ResponseEntity.ok(APIResponse.success("Progress updated successfully", roadmap));
    }

    @PatchMapping("/{id}/final-health-profile")
    @Operation(summary = "Update final health profile ID", description = "Update finalHealthProfileId for a roadmap. Only allowed when roadmap progress is 100%.")
    @ApiResponse(responseCode = "200", description = "Final health profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Roadmap progress is not 100% or request is invalid")
    @ApiResponse(responseCode = "404", description = "Roadmap or health profile not found")
    public ResponseEntity<APIResponse<RoadmapDto>> updateFinalHealthProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFinalHealthProfileRequest request) {
        RoadmapDto roadmap = roadmapService.updateFinalHealthProfile(id, request.finalHealthProfileId());
        return ResponseEntity.ok(APIResponse.success("Final health profile updated successfully", roadmap));
    }

    @GetMapping("/{id}/initial-health-profile")
    @Operation(summary = "Get initial health profile ID", description = "Get initialHealthProfileId stored on a roadmap")
    @ApiResponse(responseCode = "200", description = "Initial health profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<UUID>> getInitialHealthProfileId(@PathVariable UUID id) {
        UUID initialHealthProfileId = roadmapService.getInitialHealthProfileId(id);
        return ResponseEntity.ok(APIResponse.success("Initial health profile retrieved successfully", initialHealthProfileId));
    }

    @GetMapping("/{id}/final-health-profile")
    @Operation(summary = "Get final health profile ID", description = "Get finalHealthProfileId stored on a roadmap")
    @ApiResponse(responseCode = "200", description = "Final health profile retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<UUID>> getFinalHealthProfileId(@PathVariable UUID id) {
        UUID finalHealthProfileId = roadmapService.getFinalHealthProfileId(id);
        return ResponseEntity.ok(APIResponse.success("Final health profile retrieved successfully", finalHealthProfileId));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Approve roadmap", description = "Approve a roadmap created by a coach (changes status from PENDING to IN_PROGRESS)")
    @ApiResponse(responseCode = "200", description = "Roadmap approved successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Roadmap is not in PENDING status or user is not a trainee")
    public ResponseEntity<APIResponse<RoadmapDto>> approveRoadmap(@PathVariable UUID id) {
        RoadmapDto roadmap = roadmapService.approveRoadmap(id);
        return ResponseEntity.ok(APIResponse.success("Roadmap approved successfully", roadmap));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate roadmap", description = "Mark a roadmap as cancelled. Coaches cannot cancel roadmaps with IN_PROGRESS status.")
    @ApiResponse(responseCode = "200", description = "Roadmap deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "400", description = "Coach attempted to cancel an IN_PROGRESS roadmap")
    public ResponseEntity<APIResponse<Void>> deactivateRoadmap(@PathVariable UUID id) {
        roadmapService.deactivateRoadmap(id);
        return ResponseEntity.ok(APIResponse.success("Roadmap deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete roadmap", description = "Permanently delete a roadmap")
    @ApiResponse(responseCode = "200", description = "Roadmap deleted successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<Void>> deleteRoadmap(@PathVariable UUID id) {
        roadmapService.deleteRoadmap(id);
        return ResponseEntity.ok(APIResponse.success("Roadmap deleted successfully", null));
    }
}

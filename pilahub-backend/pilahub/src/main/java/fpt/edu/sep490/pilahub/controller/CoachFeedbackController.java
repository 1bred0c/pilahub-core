package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CoachFeedbackDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CoachFeedbackService;
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
@RequestMapping("/api/coach-feedbacks")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coach Feedback", description = "Manage feedback and ratings for coaches")
public class CoachFeedbackController {

    private final CoachFeedbackService coachFeedbackService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Create coach feedback", description = "Submit feedback for a coach")
    @ApiResponse(responseCode = "201", description = "Feedback created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CoachFeedbackDto>> createFeedback(
            @Valid @RequestBody CreateCoachFeedbackRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        CoachFeedbackDto feedback = coachFeedbackService.createFeedback(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Feedback submitted successfully", feedback));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get feedback by ID", description = "Retrieve a specific feedback by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Feedback not found")
    public ResponseEntity<APIResponse<CoachFeedbackDto>> getFeedbackById(@PathVariable("id") UUID feedbackId) {
        CoachFeedbackDto feedback = coachFeedbackService.getById(feedbackId);
        return ResponseEntity.ok(APIResponse.success("Feedback retrieved successfully", feedback));
    }

    @GetMapping("/coach/{coachId}")
    @Operation(summary = "Get feedbacks by coach ID", description = "Retrieve all feedbacks for a specific coach")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachFeedbackDto>>> getFeedbacksByCoachId(
            @PathVariable("coachId") UUID coachId) {
        List<CoachFeedbackDto> feedbacks = coachFeedbackService.getByCoachId(coachId);
        return ResponseEntity.ok(APIResponse.success("Feedbacks retrieved successfully", feedbacks));
    }

    @GetMapping("/coach/{coachId}/average-rating")
    @Operation(summary = "Get average rating for coach", description = "Get the average rating for a specific coach")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<Double>> getAverageRating(@PathVariable("coachId") UUID coachId) {
        Double averageRating = coachFeedbackService.getAverageRatingByCoachId(coachId);
        return ResponseEntity.ok(APIResponse.success("Average rating calculated successfully", averageRating));
    }

    @GetMapping("/trainee/me")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my feedbacks", description = "Retrieve all feedbacks submitted by the current trainee")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<CoachFeedbackDto>>> getMyFeedbacks() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<CoachFeedbackDto> feedbacks = coachFeedbackService.getByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success("Feedbacks retrieved successfully", feedbacks));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Update feedback", description = "Update your own feedback")
    @ApiResponse(responseCode = "200", description = "Feedback updated successfully")
    @ApiResponse(responseCode = "404", description = "Feedback not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CoachFeedbackDto>> updateFeedback(
            @PathVariable("id") UUID feedbackId,
            @Valid @RequestBody UpdateCoachFeedbackRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        CoachFeedbackDto feedback = coachFeedbackService.updateFeedback(feedbackId, traineeId, request);
        return ResponseEntity.ok(APIResponse.success("Feedback updated successfully", feedback));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete feedback", description = "Delete your own feedback")
    @ApiResponse(responseCode = "200", description = "Feedback deleted successfully")
    @ApiResponse(responseCode = "404", description = "Feedback not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deleteFeedback(@PathVariable("id") UUID feedbackId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        coachFeedbackService.deleteFeedback(feedbackId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Feedback deleted successfully", null));
    }
}

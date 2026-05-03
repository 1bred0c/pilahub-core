package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.WorkoutFeedbackDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.WorkoutFeedbackService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workout-feedback")
@RequiredArgsConstructor
@Tag(name = "Workout Feedback", description = "AI-powered workout feedback management endpoints")
public class WorkoutFeedbackController {

    private final WorkoutFeedbackService workoutFeedbackService;
    private final SecurityUtil securityUtil;

    @PostMapping("/generate/{workoutSessionId}")
    @Operation(
            summary = "Generate AI feedback for workout session",
            description = "Generate AI-powered feedback for a completed workout session. " +
                    "The workout session must have AI tracking enabled (haveAITracking = true) and be completed."
    )
    @ApiResponse(responseCode = "201", description = "Feedback generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request - session not completed or AI tracking disabled")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    @ApiResponse(responseCode = "409", description = "Feedback already exists for this session")
    public ResponseEntity<APIResponse<WorkoutFeedbackDto>> generateFeedback(@PathVariable UUID workoutSessionId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        WorkoutFeedbackDto feedback = workoutFeedbackService.generateFeedback(workoutSessionId, traineeId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Workout feedback generated successfully", feedback));
    }

    @GetMapping("/session/{workoutSessionId}")
    @Operation(
            summary = "Get feedback by workout session ID",
            description = "Retrieve AI-generated feedback for a specific workout session"
    )
    @ApiResponse(responseCode = "200", description = "Feedback retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Feedback not found")
    public ResponseEntity<APIResponse<WorkoutFeedbackDto>> getFeedbackByWorkoutSessionId(
            @PathVariable UUID workoutSessionId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        WorkoutFeedbackDto feedback = workoutFeedbackService.getFeedbackByWorkoutSessionId(workoutSessionId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Feedback retrieved successfully", feedback));
    }

    @GetMapping("/my-feedback")
    @Operation(
            summary = "Get all my workout feedback",
            description = "Retrieve all AI-generated feedback for the current trainee's workout sessions"
    )
    @ApiResponse(responseCode = "200", description = "Feedback list retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkoutFeedbackDto>>> getMyFeedback() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<WorkoutFeedbackDto> feedbackList = workoutFeedbackService.getAllFeedbackByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success("Feedback list retrieved successfully", feedbackList));
    }

    @DeleteMapping("/session/{workoutSessionId}")
    @Operation(
            summary = "Delete feedback by workout session ID",
            description = "Delete AI-generated feedback for a specific workout session"
    )
    @ApiResponse(responseCode = "200", description = "Feedback deleted successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Feedback not found")
    public ResponseEntity<APIResponse<Void>> deleteFeedback(@PathVariable UUID workoutSessionId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        workoutFeedbackService.deleteFeedback(workoutSessionId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Feedback deleted successfully", null));
    }
}



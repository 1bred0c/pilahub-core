package pilahub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilahub.dto.request.WorkoutFeedbackAIRequest;
import pilahub.dto.request.WorkoutFeedbackAnalysisRequest;
import pilahub.dto.response.WorkoutFeedbackAnalysisResponse;
import pilahub.service.WorkoutFeedbackService;

@RestController
@RequestMapping("/api/v1/workout-feedback")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Workout Feedback", description = "AI-powered workout feedback and analysis endpoints")
public class WorkoutFeedbackController {

    private final WorkoutFeedbackService workoutFeedbackService;

    @PostMapping("/analyze")
    @Deprecated
    @Operation(summary = "Analyze workout feedback (OLD)",
               description = "Legacy endpoint for backward compatibility. Use /analyze-ai instead.")
    public ResponseEntity<WorkoutFeedbackAnalysisResponse> analyzeWorkoutFeedback(
            @Valid @RequestBody WorkoutFeedbackAnalysisRequest request) {

        log.info("Received workout feedback analysis request for exercise: {}", request.getExerciseName());

        try {
            WorkoutFeedbackAnalysisResponse response = workoutFeedbackService.analyzeWorkoutFeedback(request);

            log.info("Successfully completed workout feedback analysis. Form Score: {}, Overall Score: {}",
                    response.getFormScore(), response.getOverallScore());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing workout feedback analysis: {}", e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/analyze-ai")
    @Operation(summary = "Analyze workout feedback with AI (NEW)",
               description = "Comprehensive workout feedback analysis using new input structure from Backend Server with File Search integration for reference guidelines.")
    public ResponseEntity<WorkoutFeedbackAnalysisResponse> analyzeWorkoutFeedbackAI(
            @Valid @RequestBody WorkoutFeedbackAIRequest request) {

        log.info("Received AI workout feedback analysis request for session: {}, exercise: {}",
                request.getWorkoutSessionId(), request.getExerciseInfo().getName());

        try {
            WorkoutFeedbackAnalysisResponse response = workoutFeedbackService.analyzeWorkoutFeedbackAI(request);

            log.info("Successfully completed AI workout feedback analysis. Session: {}, Form Score: {}, Overall Score: {}",
                    request.getWorkoutSessionId(), response.getFormScore(), response.getOverallScore());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing AI workout feedback analysis for session {}: {}",
                    request.getWorkoutSessionId(), e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if Workout Feedback Service is operational")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Workout Feedback Service is running");
    }
}




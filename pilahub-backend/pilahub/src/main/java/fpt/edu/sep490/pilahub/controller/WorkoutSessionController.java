package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.WorkoutSessionDto;
import fpt.edu.sep490.pilahub.dto.request.CompleteWorkoutSessionRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartFreeWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartLessonExerciseWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.StartPersonalExerciseWorkoutRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.WorkoutSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workout-sessions")
@RequiredArgsConstructor
@Tag(name = "Workout Session", description = "Workout session tracking endpoints for trainees")
public class WorkoutSessionController {

    private final WorkoutSessionService workoutSessionService;

    @PostMapping("/start/personal-exercise")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Start workout for personal exercise", description = "Start a workout session for a personal exercise (exercise is auto-retrieved from PersonalExercise)")
    @ApiResponse(responseCode = "201", description = "Workout session started successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Personal exercise not found")
    public ResponseEntity<APIResponse<WorkoutSessionDto>> startPersonalExerciseWorkout(
            @Valid @RequestBody StartPersonalExerciseWorkoutRequest request) {
        WorkoutSessionDto workoutSession = workoutSessionService.startPersonalExerciseWorkout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal exercise workout started successfully", workoutSession));
    }

    @PostMapping("/start/lesson-exercise")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Start workout for lesson exercise", description = "Start a workout session for a lesson exercise in course (exercise is auto-retrieved from LessonExercise)")
    @ApiResponse(responseCode = "201", description = "Workout session started successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or lesson exercise doesn't belong to lesson")
    @ApiResponse(responseCode = "404", description = "Course lesson progress or lesson exercise not found")
    public ResponseEntity<APIResponse<WorkoutSessionDto>> startLessonExerciseWorkout(
            @Valid @RequestBody StartLessonExerciseWorkoutRequest request) {
        WorkoutSessionDto workoutSession = workoutSessionService.startLessonExerciseWorkout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Lesson exercise workout started successfully", workoutSession));
    }

    @PostMapping("/start/free")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Start free workout", description = "Start a standalone workout session (not linked to any plan or course)")
    @ApiResponse(responseCode = "201", description = "Workout session started successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<WorkoutSessionDto>> startFreeWorkout(
            @Valid @RequestBody StartFreeWorkoutRequest request) {
        WorkoutSessionDto workoutSession = workoutSessionService.startFreeWorkout(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Free workout started successfully", workoutSession));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Complete workout session",
            description = "Mark a workout session as completed. Requires recording URL from the client. " +
                    "End time and record availability will be set automatically by the system. " +
                    "Heart rate logs and mistake logs should be created using their respective separate endpoints before completing the session."
    )
    @ApiResponse(responseCode = "200", description = "Workout session completed successfully")
    @ApiResponse(responseCode = "400", description = "Session already completed or invalid record URL")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<WorkoutSessionDto>> completeWorkoutSession(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteWorkoutSessionRequest request) {
        WorkoutSessionDto workoutSession = workoutSessionService.completeWorkoutSession(id, request);
        return ResponseEntity.ok(APIResponse.success("Workout session completed successfully", workoutSession));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get workout session by ID", description = "Retrieve a specific workout session by its ID")
    @ApiResponse(responseCode = "200", description = "Workout session found")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<WorkoutSessionDto>> getWorkoutSessionById(@PathVariable UUID id) {
        WorkoutSessionDto workoutSession = workoutSessionService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Workout session retrieved successfully", workoutSession));
    }

    @GetMapping("/my-sessions")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my workout sessions", description = "Retrieve all workout sessions for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Workout sessions retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getMyWorkoutSessions() {
        List<WorkoutSessionDto> sessions = workoutSessionService.getMyWorkoutSessions();
        return ResponseEntity.ok(APIResponse.success("Workout sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions/completed")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my completed sessions", description = "Retrieve all completed workout sessions for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Completed sessions retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getMyCompletedSessions() {
        List<WorkoutSessionDto> sessions = workoutSessionService.getMyCompletedWorkoutSessions();
        return ResponseEntity.ok(APIResponse.success("Completed sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions/incomplete")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get my incomplete sessions", description = "Retrieve all incomplete workout sessions for the authenticated trainee")
    @ApiResponse(responseCode = "200", description = "Incomplete sessions retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getMyIncompleteSessions() {
        List<WorkoutSessionDto> sessions = workoutSessionService.getMyIncompleteWorkoutSessions();
        return ResponseEntity.ok(APIResponse.success("Incomplete sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions/by-date-range")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get sessions by date range", description = "Retrieve workout sessions within a specific date range")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getSessionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<WorkoutSessionDto> sessions = workoutSessionService.getWorkoutSessionsByDateRange(from, to);
        return ResponseEntity.ok(APIResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions/by-exercise/{exerciseId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get sessions by exercise", description = "Retrieve workout sessions for a specific exercise")
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getSessionsByExercise(@PathVariable UUID exerciseId) {
        List<WorkoutSessionDto> sessions = workoutSessionService.getWorkoutSessionsByExercise(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Sessions retrieved successfully", sessions));
    }

    @GetMapping("/my-sessions/by-exercise/{exerciseId}/with-filters")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get sessions by exercise with filters",
            description = "Retrieve workout sessions for a specific exercise with optional filters. " +
                    "Parameters lessonExerciseProgressId, personalExerciseId are all optional. " +
                    "If both are null, returns only free workouts (not linked to any lesson or personal exercise)."
    )
    @ApiResponse(responseCode = "200", description = "Sessions retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<List<WorkoutSessionDto>>> getSessionsByExerciseWithFilters(
            @PathVariable UUID exerciseId,
            @RequestParam(required = false) UUID lessonExerciseProgressId,
            @RequestParam(required = false) UUID personalExerciseId) {
        List<WorkoutSessionDto> sessions = workoutSessionService.getWorkoutSessionsByExerciseWithFilters(
                exerciseId, lessonExerciseProgressId, personalExerciseId);
        return ResponseEntity.ok(APIResponse.success("Sessions retrieved successfully", sessions));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete workout session", description = "Permanently delete a workout session")
    @ApiResponse(responseCode = "200", description = "Workout session deleted successfully")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<Void>> deleteWorkoutSession(@PathVariable UUID id) {
        workoutSessionService.deleteWorkoutSession(id);
        return ResponseEntity.ok(APIResponse.success("Workout session deleted successfully", null));
    }
}


package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.ExerciseService;
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
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
@Tag(name = "Exercise", description = "Exercise management endpoints")
public class ExerciseController {

    private final ExerciseService exerciseService;

    @PostMapping
    @Operation(summary = "Create exercise", description = "Create a new exercise")
    @ApiResponse(responseCode = "201", description = "Exercise created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<ExerciseDto>> createExercise(@Valid @RequestBody CreateExerciseRequest request) {
        ExerciseDto exercise = exerciseService.createExercise(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Exercise created successfully", exercise));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise by ID", description = "Retrieve a specific exercise by its ID")
    @ApiResponse(responseCode = "200", description = "Exercise found")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<ExerciseDto>> getExerciseById(@PathVariable UUID id) {
        ExerciseDto exercise = exerciseService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Exercise retrieved successfully", exercise));
    }

    @GetMapping
    @Operation(summary = "Get all exercises", description = "Retrieve all exercises (active and inactive)")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseDto>>> getAllExercises() {
        List<ExerciseDto> exercises = exerciseService.getAll();
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", exercises));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active exercises", description = "Retrieve all active exercises")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseDto>>> getAllActiveExercises() {
        List<ExerciseDto> exercises = exerciseService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", exercises));
    }

    @GetMapping("/search")
    @Operation(summary = "Search exercises by name", description = "Search for exercises by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<ExerciseDto>>> searchExercises(@RequestParam String name) {
        List<ExerciseDto> exercises = exerciseService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", exercises));
    }

    @GetMapping("/difficulty/{level}")
    @Operation(summary = "Get exercises by difficulty", description = "Retrieve active exercises filtered by difficulty level")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseDto>>> getExercisesByDifficulty(@PathVariable String level) {
        List<ExerciseDto> exercises = exerciseService.getActiveByDifficultyLevel(level);
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", exercises));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exercise", description = "Update an existing exercise")
    @ApiResponse(responseCode = "200", description = "Exercise updated successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<ExerciseDto>> updateExercise(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExerciseRequest request) {
        ExerciseDto exercise = exerciseService.updateExercise(id, request);
        return ResponseEntity.ok(APIResponse.success("Exercise updated successfully", exercise));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate exercise", description = "Mark an exercise as active")
    @ApiResponse(responseCode = "200", description = "Exercise activated successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<Void>> activateExercise(@PathVariable UUID id) {
        exerciseService.activateExercise(id);
        return ResponseEntity.ok(APIResponse.success("Exercise activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate exercise", description = "Mark an exercise as inactive")
    @ApiResponse(responseCode = "200", description = "Exercise deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<Void>> deactivateExercise(@PathVariable UUID id) {
        exerciseService.deactivateExercise(id);
        return ResponseEntity.ok(APIResponse.success("Exercise deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exercise", description = "Permanently delete an exercise")
    @ApiResponse(responseCode = "200", description = "Exercise deleted successfully")
    @ApiResponse(responseCode = "404", description = "Exercise not found")
    public ResponseEntity<APIResponse<Void>> deleteExercise(@PathVariable UUID id) {
        exerciseService.deleteExercise(id);
        return ResponseEntity.ok(APIResponse.success("Exercise deleted successfully", null));
    }
}

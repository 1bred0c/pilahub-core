package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.LessonExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.LessonExerciseService;
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
@RequestMapping("/api/lesson-exercises")
@RequiredArgsConstructor
@Tag(name = "Lesson Exercise", description = "Lesson-exercise relationship management endpoints")
public class LessonExerciseController {

    private final LessonExerciseService lessonExerciseService;

    @PostMapping("/lesson/{lessonId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Add exercises to lesson", description = "Add multiple exercises to a lesson")
    @ApiResponse(responseCode = "201", description = "Exercises added to lesson successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Lesson or exercise not found")
    public ResponseEntity<APIResponse<List<LessonExerciseDto>>> addExercisesToLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody List<CreateLessonExerciseRequest> requests) {
        List<LessonExerciseDto> lessonExercises = lessonExerciseService.createLessonExercise(lessonId, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Exercises added to lesson successfully", lessonExercises));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson-exercise by ID", description = "Retrieve a specific lesson-exercise relationship by its ID")
    @ApiResponse(responseCode = "200", description = "Lesson-exercise found")
    @ApiResponse(responseCode = "404", description = "Lesson-exercise not found")
    public ResponseEntity<APIResponse<LessonExerciseDto>> getLessonExerciseById(@PathVariable UUID id) {
        LessonExerciseDto lessonExercise = lessonExerciseService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Lesson-exercise retrieved successfully", lessonExercise));
    }

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get exercises by lesson", description = "Retrieve all exercises for a specific lesson")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<LessonExerciseDto>>> getExercisesByLesson(@PathVariable UUID lessonId) {
        List<LessonExerciseDto> lessonExercises = lessonExerciseService.getByLessonId(lessonId);
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", lessonExercises));
    }

    @GetMapping("/lesson/{lessonId}/ordered")
    @Operation(summary = "Get exercises by lesson (ordered)", description = "Retrieve all exercises for a specific lesson, ordered by display order")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<LessonExerciseDto>>> getExercisesByLessonOrdered(@PathVariable UUID lessonId) {
        List<LessonExerciseDto> lessonExercises = lessonExerciseService.getByLessonIdOrdered(lessonId);
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", lessonExercises));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update lesson-exercise", description = "Update an existing lesson-exercise relationship")
    @ApiResponse(responseCode = "200", description = "Lesson-exercise updated successfully")
    @ApiResponse(responseCode = "404", description = "Lesson-exercise not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<LessonExerciseDto>> updateLessonExercise(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonExerciseRequest request) {
        LessonExerciseDto lessonExercise = lessonExerciseService.updateLessonExercise(id, request);
        return ResponseEntity.ok(APIResponse.success("Lesson-exercise updated successfully", lessonExercise));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete lesson-exercise", description = "Remove an exercise from a lesson")
    @ApiResponse(responseCode = "200", description = "Exercise removed from lesson successfully")
    @ApiResponse(responseCode = "404", description = "Lesson-exercise not found")
    public ResponseEntity<APIResponse<Void>> deleteLessonExercise(@PathVariable UUID id) {
        lessonExerciseService.deleteLessonExercise(id);
        return ResponseEntity.ok(APIResponse.success("Exercise removed from lesson successfully", null));
    }

    @DeleteMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all exercises from lesson", description = "Remove all exercises from a specific lesson")
    @ApiResponse(responseCode = "200", description = "All exercises removed from lesson successfully")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public ResponseEntity<APIResponse<Void>> deleteAllExercisesFromLesson(@PathVariable UUID lessonId) {
        lessonExerciseService.deleteByLessonId(lessonId);
        return ResponseEntity.ok(APIResponse.success("All exercises removed from lesson successfully", null));
    }
}

package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.LessonExerciseProgressDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.LessonExerciseProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lesson-exercise-progress")
@RequiredArgsConstructor
@Tag(name = "Lesson Exercise Progress", description = "Track progress of individual exercises within a lesson")
public class LessonExerciseProgressController {

    private final LessonExerciseProgressService lessonExerciseProgressService;

    @GetMapping("/course-lesson-progress/{courseLessonProgressId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get exercise progress by course lesson", description = "Retrieve all exercise progress for a specific course lesson")
    @ApiResponse(responseCode = "200", description = "Exercise progress retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Course lesson progress not found")
    public ResponseEntity<APIResponse<List<LessonExerciseProgressDto>>> getByCourseLessonProgress(
            @PathVariable UUID courseLessonProgressId) {
        List<LessonExerciseProgressDto> progress = lessonExerciseProgressService.getByCourseLessonProgressId(courseLessonProgressId);
        return ResponseEntity.ok(APIResponse.success("Exercise progress retrieved successfully", progress));
    }

    @GetMapping("/course-lesson-progress/{courseLessonProgressId}/completed")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get completed exercises", description = "Retrieve all completed exercises for a course lesson")
    @ApiResponse(responseCode = "200", description = "Completed exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<LessonExerciseProgressDto>>> getCompletedExercises(
            @PathVariable UUID courseLessonProgressId) {
        List<LessonExerciseProgressDto> progress = lessonExerciseProgressService.getCompletedByCourseLessonProgressId(courseLessonProgressId);
        return ResponseEntity.ok(APIResponse.success("Completed exercises retrieved successfully", progress));
    }

    @GetMapping("/course-lesson-progress/{courseLessonProgressId}/incomplete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get incomplete exercises", description = "Retrieve all incomplete exercises for a course lesson")
    @ApiResponse(responseCode = "200", description = "Incomplete exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<LessonExerciseProgressDto>>> getIncompleteExercises(
            @PathVariable UUID courseLessonProgressId) {
        List<LessonExerciseProgressDto> progress = lessonExerciseProgressService.getIncompleteByCourseLessonProgressId(courseLessonProgressId);
        return ResponseEntity.ok(APIResponse.success("Incomplete exercises retrieved successfully", progress));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get exercise progress by ID", description = "Retrieve a specific exercise progress by its ID")
    @ApiResponse(responseCode = "200", description = "Exercise progress found")
    @ApiResponse(responseCode = "404", description = "Exercise progress not found")
    public ResponseEntity<APIResponse<LessonExerciseProgressDto>> getById(@PathVariable UUID id) {
        LessonExerciseProgressDto progress = lessonExerciseProgressService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Exercise progress retrieved successfully", progress));
    }
}


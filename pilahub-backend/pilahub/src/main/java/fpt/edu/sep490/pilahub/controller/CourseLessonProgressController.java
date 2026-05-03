package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CourseLessonProgressService;
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
@RequestMapping("/api/course-lesson-progress")
@RequiredArgsConstructor
@Tag(name = "Course Lesson Progress", description = "Course lesson progress tracking endpoints")
public class CourseLessonProgressController {

    private final CourseLessonProgressService courseLessonProgressService;

    @PostMapping("/schedule")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Create lesson schedule", description = "Create progress schedule for all lessons in a trainee's course based on training days")
    @ApiResponse(responseCode = "201", description = "Schedule created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or schedule already exists")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> createSchedule(
            @Valid @RequestBody CreateCourseLessonProgressRequest request) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService.createProgress(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Lesson schedule created successfully", progress));
    }

    @PutMapping("/schedule/reset")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Reset and regenerate schedule", description = "Reset all lesson progress fields and regenerate a new schedule for the trainee's course")
    @ApiResponse(responseCode = "200", description = "Progress reset and schedule regenerated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> resetAndReschedule(
            @Valid @RequestBody CreateCourseLessonProgressRequest request) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService.resetProgressAndReschedule(request);
        return ResponseEntity.ok(APIResponse.success("Progress reset and schedule regenerated successfully", progress));
    }

    @PutMapping("/schedule/reschedule-incomplete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Reschedule incomplete lessons", description = "Regenerate schedule only for lesson progress records where completed is false")
    @ApiResponse(responseCode = "200", description = "Incomplete lessons rescheduled successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> rescheduleIncomplete(
            @Valid @RequestBody CreateCourseLessonProgressRequest request) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService.rescheduleIncompleteProgress(request);
        return ResponseEntity.ok(APIResponse.success("Incomplete lessons rescheduled successfully", progress));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get progress by ID", description = "Retrieve a specific lesson progress record by its ID")
    @ApiResponse(responseCode = "200", description = "Progress found")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public ResponseEntity<APIResponse<CourseLessonProgressDto>> getProgressById(@PathVariable UUID id) {
        CourseLessonProgressDto progress = courseLessonProgressService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Progress retrieved successfully", progress));
    }

    @GetMapping("/trainee-course/{traineeCourseId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get progress by trainee course", description = "Retrieve all lesson progress for a specific trainee course")
    @ApiResponse(responseCode = "200", description = "Progress retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> getProgressByTraineeCourse(
            @PathVariable UUID traineeCourseId) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService.getByTraineeCourseId(traineeCourseId);
        return ResponseEntity.ok(APIResponse.success("Progress retrieved successfully", progress));
    }

    @GetMapping("/course-lesson/{courseLessonId}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get progress by course lesson", description = "Retrieve all progress records for a specific course lesson")
    @ApiResponse(responseCode = "200", description = "Progress retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Course lesson not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> getProgressByCourseLesson(
            @PathVariable UUID courseLessonId) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService.getByCourseLessonId(courseLessonId);
        return ResponseEntity.ok(APIResponse.success("Progress retrieved successfully", progress));
    }

    @GetMapping("/trainee-course/{traineeCourseId}/course-lesson/{courseLessonId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get specific lesson progress", description = "Retrieve progress for a specific lesson in a trainee's course")
    @ApiResponse(responseCode = "200", description = "Progress found")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public ResponseEntity<APIResponse<CourseLessonProgressDto>> getProgressByTraineeCourseAndLesson(
            @PathVariable UUID traineeCourseId,
            @PathVariable UUID courseLessonId) {
        CourseLessonProgressDto progress = courseLessonProgressService
                .getByTraineeCourseIdAndCourseLessonId(traineeCourseId, courseLessonId);
        return ResponseEntity.ok(APIResponse.success("Progress retrieved successfully", progress));
    }

    @GetMapping("/trainee-course/{traineeCourseId}/completed")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get completed lessons", description = "Retrieve all completed lessons for a trainee's course")
    @ApiResponse(responseCode = "200", description = "Completed lessons retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> getCompletedLessons(
            @PathVariable UUID traineeCourseId) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService
                .getCompletedByTraineeCourseId(traineeCourseId);
        return ResponseEntity.ok(APIResponse.success("Completed lessons retrieved successfully", progress));
    }

    @GetMapping("/trainee-course/{traineeCourseId}/incomplete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get incomplete lessons", description = "Retrieve all incomplete lessons for a trainee's course")
    @ApiResponse(responseCode = "200", description = "Incomplete lessons retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<List<CourseLessonProgressDto>>> getIncompleteLessons(
            @PathVariable UUID traineeCourseId) {
        List<CourseLessonProgressDto> progress = courseLessonProgressService
                .getIncompleteByTraineeCourseId(traineeCourseId);
        return ResponseEntity.ok(APIResponse.success("Incomplete lessons retrieved successfully", progress));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Update progress", description = "Update a lesson progress record")
    @ApiResponse(responseCode = "200", description = "Progress updated successfully")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<CourseLessonProgressDto>> updateProgress(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseLessonProgressRequest request) {
        CourseLessonProgressDto progress = courseLessonProgressService.updateProgress(id, request);
        return ResponseEntity.ok(APIResponse.success("Progress updated successfully", progress));
    }

    @PutMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Start lesson", description = "Mark a lesson as started")
    @ApiResponse(responseCode = "200", description = "Lesson started successfully")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public ResponseEntity<APIResponse<CourseLessonProgressDto>> startLesson(@PathVariable UUID id) {
        CourseLessonProgressDto progress = courseLessonProgressService.startLesson(id);
        return ResponseEntity.ok(APIResponse.success("Lesson started successfully", progress));
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Complete lesson", description = "Mark a lesson as completed")
    @ApiResponse(responseCode = "200", description = "Lesson marked as completed successfully")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public ResponseEntity<APIResponse<CourseLessonProgressDto>> markAsCompleted(@PathVariable UUID id) {
        CourseLessonProgressDto progress = courseLessonProgressService.markAsCompleted(id);
        return ResponseEntity.ok(APIResponse.success("Lesson completed successfully", progress));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete progress", description = "Delete a specific lesson progress record")
    @ApiResponse(responseCode = "200", description = "Progress deleted successfully")
    @ApiResponse(responseCode = "404", description = "Progress not found")
    public ResponseEntity<APIResponse<Void>> deleteProgress(@PathVariable UUID id) {
        courseLessonProgressService.deleteProgress(id);
        return ResponseEntity.ok(APIResponse.success("Progress deleted successfully", null));
    }

    @DeleteMapping("/trainee-course/{traineeCourseId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete all progress for trainee course", description = "Delete all lesson progress records for a trainee's course")
    @ApiResponse(responseCode = "200", description = "All progress deleted successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<Void>> deleteByTraineeCourse(@PathVariable UUID traineeCourseId) {
        courseLessonProgressService.deleteByTraineeCourseId(traineeCourseId);
        return ResponseEntity.ok(APIResponse.success("All progress deleted successfully", null));
    }

    @DeleteMapping("/course-lesson/{courseLessonId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    @Operation(summary = "Delete all progress for course lesson", description = "Delete all progress records for a specific course lesson")
    @ApiResponse(responseCode = "200", description = "All progress deleted successfully")
    @ApiResponse(responseCode = "404", description = "Course lesson not found")
    public ResponseEntity<APIResponse<Void>> deleteByCourseLesson(@PathVariable UUID courseLessonId) {
        courseLessonProgressService.deleteByCourseLessonId(courseLessonId);
        return ResponseEntity.ok(APIResponse.success("All progress deleted successfully", null));
    }

    @GetMapping("/exists")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Check progress existence", description = "Check if progress record exists for a specific trainee course and lesson")
    @ApiResponse(responseCode = "200", description = "Existence status retrieved")
    public ResponseEntity<APIResponse<Boolean>> checkProgressExists(
            @RequestParam UUID traineeCourseId,
            @RequestParam UUID courseLessonId) {
        boolean exists = courseLessonProgressService.existsByTraineeCourseIdAndCourseLessonId(traineeCourseId,
                courseLessonId);
        return ResponseEntity.ok(APIResponse.success("Existence status retrieved", exists));
    }
}

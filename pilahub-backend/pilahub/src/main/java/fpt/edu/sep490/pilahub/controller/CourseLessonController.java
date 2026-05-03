package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CourseLessonDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CourseLessonService;
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
@RequestMapping("/api/course-lessons")
@RequiredArgsConstructor
@Tag(name = "Course Lesson", description = "Course-lesson relationship management endpoints")
public class CourseLessonController {

    private final CourseLessonService courseLessonService;

    @PostMapping("/course/{courseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Add lessons to course", description = "Add multiple lessons to a course")
    @ApiResponse(responseCode = "201", description = "Lessons added to course successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Course or lesson not found")
    public ResponseEntity<APIResponse<List<CourseLessonDto>>> addLessonsToCourse(
            @PathVariable UUID courseId,
            @Valid @RequestBody List<CreateCourseLessonRequest> requests) {
        List<CourseLessonDto> courseLessons = courseLessonService.createCourseLesson(courseId, requests);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Lessons added to course successfully", courseLessons));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course-lesson by ID", description = "Retrieve a specific course-lesson relationship by its ID")
    @ApiResponse(responseCode = "200", description = "Course-lesson found")
    @ApiResponse(responseCode = "404", description = "Course-lesson not found")
    public ResponseEntity<APIResponse<CourseLessonDto>> getCourseLessonById(@PathVariable UUID id) {
        CourseLessonDto courseLesson = courseLessonService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Course-lesson retrieved successfully", courseLesson));
    }

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get lessons by course", description = "Retrieve all lessons for a specific course")
    @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseLessonDto>>> getLessonsByCourse(@PathVariable UUID courseId) {
        List<CourseLessonDto> courseLessons = courseLessonService.getByCourseId(courseId);
        return ResponseEntity.ok(APIResponse.success("Lessons retrieved successfully", courseLessons));
    }

    @GetMapping("/course/{courseId}/ordered")
    @Operation(summary = "Get lessons by course (ordered)", description = "Retrieve all lessons for a specific course, ordered by display order")
    @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseLessonDto>>> getLessonsByCourseOrdered(@PathVariable UUID courseId) {
        List<CourseLessonDto> courseLessons = courseLessonService.getByCourseIdOrdered(courseId);
        return ResponseEntity.ok(APIResponse.success("Lessons retrieved successfully", courseLessons));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update course-lesson", description = "Update an existing course-lesson relationship")
    @ApiResponse(responseCode = "200", description = "Course-lesson updated successfully")
    @ApiResponse(responseCode = "404", description = "Course-lesson not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<CourseLessonDto>> updateCourseLesson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseLessonRequest request) {
        CourseLessonDto courseLesson = courseLessonService.updateCourseLesson(id, request);
        return ResponseEntity.ok(APIResponse.success("Course-lesson updated successfully", courseLesson));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete course-lesson", description = "Remove a lesson from a course")
    @ApiResponse(responseCode = "200", description = "Lesson removed from course successfully")
    @ApiResponse(responseCode = "404", description = "Course-lesson not found")
    public ResponseEntity<APIResponse<Void>> deleteCourseLesson(@PathVariable UUID id) {
        courseLessonService.deleteCourseLesson(id);
        return ResponseEntity.ok(APIResponse.success("Lesson removed from course successfully", null));
    }

    @DeleteMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete all lessons from course", description = "Remove all lessons from a specific course")
    @ApiResponse(responseCode = "200", description = "All lessons removed from course successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<Void>> deleteAllLessonsFromCourse(@PathVariable UUID courseId) {
        courseLessonService.deleteByCourseId(courseId);
        return ResponseEntity.ok(APIResponse.success("All lessons removed from course successfully", null));
    }
}

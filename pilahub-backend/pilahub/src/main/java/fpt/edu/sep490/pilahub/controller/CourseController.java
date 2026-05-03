package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.CourseEditDetailsResponse;
import fpt.edu.sep490.pilahub.dto.response.CourseWithDetailsResponse;
import fpt.edu.sep490.pilahub.service.CourseService;
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
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Course", description = "Course management endpoints")
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create course", description = "Create a new course")
    @ApiResponse(responseCode = "201", description = "Course created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<CourseDto>> createCourse(@Valid @RequestBody CreateCourseRequest request) {
        CourseDto course = courseService.createCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Course created successfully", course));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Retrieve a specific course by its ID")
    @ApiResponse(responseCode = "200", description = "Course found")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<CourseDto>> getCourseById(@PathVariable UUID id) {
        CourseDto course = courseService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Course retrieved successfully", course));
    }

    @GetMapping("/{id}/details")
    @Operation(summary = "Get course with full details", description = "Retrieve a course with all its lessons (via CourseLesson) and exercises (via LessonExercise)")
    @ApiResponse(responseCode = "200", description = "Course details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<CourseWithDetailsResponse>> getCourseWithDetails(@PathVariable UUID id) {
        CourseWithDetailsResponse details = courseService.getCourseWithDetails(id);
        return ResponseEntity.ok(APIResponse.success("Course details retrieved successfully", details));
    }

    @GetMapping("/{id}/edit-details")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get course edit details (admin)", description = "Retrieve full editable course structure for admin")
    @ApiResponse(responseCode = "200", description = "Course edit details retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<CourseEditDetailsResponse>> getCourseEditDetails(@PathVariable UUID id) {
        CourseEditDetailsResponse details = courseService.getCourseEditDetails(id);
        return ResponseEntity.ok(APIResponse.success("Course edit details retrieved successfully", details));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Get all courses", description = "Retrieve all courses (active and inactive)")
    @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseDto>>> getAllCourses() {
        List<CourseDto> courses = courseService.getAll();
        return ResponseEntity.ok(APIResponse.success("Courses retrieved successfully", courses));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active courses", description = "Retrieve all active courses")
    @ApiResponse(responseCode = "200", description = "Active courses retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseDto>>> getAllActiveCourses() {
        List<CourseDto> courses = courseService.getAllActive();
        return ResponseEntity.ok(APIResponse.success("Active courses retrieved successfully", courses));
    }

    @GetMapping("/search")
    @Operation(summary = "Search courses by name", description = "Search for courses by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<CourseDto>>> searchCourses(@RequestParam String name) {
        List<CourseDto> courses = courseService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", courses));
    }

    @GetMapping("/level/{level}")
    @Operation(summary = "Get courses by difficulty level", description = "Retrieve courses filtered by difficulty level")
    @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseDto>>> getCoursesByLevel(@PathVariable String level) {
        List<CourseDto> courses = courseService.getByLevel(level);
        return ResponseEntity.ok(APIResponse.success("Courses retrieved successfully", courses));
    }

    @GetMapping("/level/{level}/active")
    @Operation(summary = "Get active courses by difficulty level", description = "Retrieve active courses filtered by difficulty level")
    @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    public ResponseEntity<APIResponse<List<CourseDto>>> getActiveCoursesByLevel(@PathVariable String level) {
        List<CourseDto> courses = courseService.getActiveByLevel(level);
        return ResponseEntity.ok(APIResponse.success("Courses retrieved successfully", courses));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update course", description = "Update an existing course")
    @ApiResponse(responseCode = "200", description = "Course updated successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<CourseDto>> updateCourse(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCourseRequest request) {
        CourseDto course = courseService.updateCourse(id, request);
        return ResponseEntity.ok(APIResponse.success("Course updated successfully", course));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Activate course", description = "Mark a course as active")
    @ApiResponse(responseCode = "200", description = "Course activated successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<Void>> activateCourse(@PathVariable UUID id) {
        courseService.activateCourse(id);
        return ResponseEntity.ok(APIResponse.success("Course activated successfully", null));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Deactivate course", description = "Mark a course as inactive")
    @ApiResponse(responseCode = "200", description = "Course deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<Void>> deactivateCourse(@PathVariable UUID id) {
        courseService.deactivateCourse(id);
        return ResponseEntity.ok(APIResponse.success("Course deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete course", description = "Permanently delete a course")
    @ApiResponse(responseCode = "200", description = "Course deleted successfully")
    @ApiResponse(responseCode = "404", description = "Course not found")
    public ResponseEntity<APIResponse<Void>> deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(APIResponse.success("Course deleted successfully", null));
    }
}

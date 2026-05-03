package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TraineeCourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateTraineeCourseRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.TraineeCourseService;
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
@RequestMapping("/api/trainee-courses")
@RequiredArgsConstructor
@Tag(name = "Trainee Course", description = "Trainee course enrollment management endpoints")
public class TraineeCourseController {

    private final TraineeCourseService traineeCourseService;

    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(summary = "Enroll in course", description = "Enroll a trainee in a course")
    @ApiResponse(responseCode = "201", description = "Enrolled successfully")
    @ApiResponse(responseCode = "400", description = "Already enrolled or invalid input")
    @ApiResponse(responseCode = "404", description = "Account or course not found")
    public ResponseEntity<APIResponse<TraineeCourseDto>> enrollCourse(@Valid @RequestBody CreateTraineeCourseRequest request) {
        TraineeCourseDto traineeCourse = traineeCourseService.enrollCourse(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Enrolled in course successfully", traineeCourse));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get trainee course by ID", description = "Retrieve a specific trainee course enrollment by its ID")
    @ApiResponse(responseCode = "200", description = "Trainee course found")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<TraineeCourseDto>> getTraineeCourseById(@PathVariable UUID id) {
        TraineeCourseDto traineeCourse = traineeCourseService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Trainee course retrieved successfully", traineeCourse));
    }

    @GetMapping("/trainee/{traineeId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Get courses by trainee", description = "Retrieve all courses enrolled by a specific trainee")
    @ApiResponse(responseCode = "200", description = "Courses retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<List<TraineeCourseDto>>> getCoursesByTrainee(@PathVariable UUID traineeId) {
        List<TraineeCourseDto> traineeCourses = traineeCourseService.getByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success("Trainee courses retrieved successfully", traineeCourses));
    }

    @PutMapping("/{id}/progress")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Update course progress", description = "Update progress percentage for a trainee's course")
    @ApiResponse(responseCode = "200", description = "Progress updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid progress percentage")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<TraineeCourseDto>> updateProgress(
            @PathVariable UUID id,
            @RequestParam Integer progressPercentage) {
        TraineeCourseDto traineeCourse = traineeCourseService.updateProgress(id, progressPercentage);
        return ResponseEntity.ok(APIResponse.success("Progress updated successfully", traineeCourse));
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Activate trainee course", description = "Activate a trainee's course enrollment")
    @ApiResponse(responseCode = "200", description = "Course activated successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<TraineeCourseDto>> activateTraineeCourse(@PathVariable UUID id) {
        TraineeCourseDto traineeCourse = traineeCourseService.activateTraineeCourse(id);
        return ResponseEntity.ok(APIResponse.success("Course activated successfully", traineeCourse));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'ADMIN')")
    @Operation(summary = "Delete trainee course", description = "Remove a trainee's course enrollment")
    @ApiResponse(responseCode = "200", description = "Enrollment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Trainee course not found")
    public ResponseEntity<APIResponse<Void>> deleteTraineeCourse(@PathVariable UUID id) {
        traineeCourseService.deleteTraineeCourse(id);
        return ResponseEntity.ok(APIResponse.success("Enrollment deleted successfully", null));
    }

    @GetMapping("/check-enrollment")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(summary = "Check enrollment status", description = "Check if a trainee is enrolled in a specific course")
    @ApiResponse(responseCode = "200", description = "Enrollment status retrieved")
    public ResponseEntity<APIResponse<Boolean>> checkEnrollment(
            @RequestParam UUID traineeId,
            @RequestParam UUID courseId) {
        boolean isEnrolled = traineeCourseService.isEnrolled(traineeId, courseId);
        return ResponseEntity.ok(APIResponse.success("Enrollment status retrieved", isEnrolled));
    }
}

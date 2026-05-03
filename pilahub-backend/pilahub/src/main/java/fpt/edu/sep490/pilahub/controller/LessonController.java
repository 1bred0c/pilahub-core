package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.LessonDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.LessonService;
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
@RequestMapping("/api/lessons")
@RequiredArgsConstructor
@Tag(name = "Lesson", description = "Lesson management endpoints")
public class LessonController {

    private final LessonService lessonService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create lesson", description = "Create a new lesson")
    @ApiResponse(responseCode = "201", description = "Lesson created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<LessonDto>> createLesson(@Valid @RequestBody CreateLessonRequest request) {
        LessonDto lesson = lessonService.createLesson(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Lesson created successfully", lesson));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get lesson by ID", description = "Retrieve a specific lesson by its ID")
    @ApiResponse(responseCode = "200", description = "Lesson found")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public ResponseEntity<APIResponse<LessonDto>> getLessonById(@PathVariable UUID id) {
        LessonDto lesson = lessonService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Lesson retrieved successfully", lesson));
    }

    @GetMapping
    @Operation(summary = "Get all active lessons", description = "Retrieve all active lessons")
    @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    public ResponseEntity<APIResponse<List<LessonDto>>> getAllLessons() {
        List<LessonDto> lessons = lessonService.getAll();
        return ResponseEntity.ok(APIResponse.success("Lessons retrieved successfully", lessons));
    }

    @GetMapping("/search")
    @Operation(summary = "Search lessons by name", description = "Search for lessons by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<LessonDto>>> searchLessons(@RequestParam String name) {
        List<LessonDto> lessons = lessonService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", lessons));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update lesson", description = "Update an existing lesson")
    @ApiResponse(responseCode = "200", description = "Lesson updated successfully")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<LessonDto>> updateLesson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLessonRequest request) {
        LessonDto lesson = lessonService.updateLesson(id, request);
        return ResponseEntity.ok(APIResponse.success("Lesson updated successfully", lesson));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Deactivate lesson", description = "Mark a lesson as inactive")
    @ApiResponse(responseCode = "200", description = "Lesson deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public ResponseEntity<APIResponse<Void>> deactivateLesson(@PathVariable UUID id) {
        lessonService.deactivateLesson(id);
        return ResponseEntity.ok(APIResponse.success("Lesson deactivated successfully", null));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete lesson", description = "Permanently delete a lesson")
    @ApiResponse(responseCode = "200", description = "Lesson deleted successfully")
    @ApiResponse(responseCode = "404", description = "Lesson not found")
    public ResponseEntity<APIResponse<Void>> deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.ok(APIResponse.success("Lesson deleted successfully", null));
    }
}

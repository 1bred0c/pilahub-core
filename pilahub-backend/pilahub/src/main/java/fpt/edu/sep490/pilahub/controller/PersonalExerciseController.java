package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalExerciseRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PersonalExerciseService;
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
@RequestMapping("/api/personal-exercises")
@RequiredArgsConstructor
@Tag(name = "Personal Exercise", description = "Personal exercise management endpoints")
public class PersonalExerciseController {

    private final PersonalExerciseService personalExerciseService;

    @PostMapping
    @Operation(summary = "Create personal exercise", description = "Create a new personal exercise for a schedule")
    @ApiResponse(responseCode = "201", description = "Personal exercise created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Personal schedule or exercise not found")
    public ResponseEntity<APIResponse<PersonalExerciseDto>> createPersonalExercise(@Valid @RequestBody CreatePersonalExerciseRequest request) {
        PersonalExerciseDto personalExercise = personalExerciseService.createPersonalExercise(request );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal exercise created successfully", personalExercise));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get personal exercise by ID", description = "Retrieve a specific personal exercise by its ID")
    @ApiResponse(responseCode = "200", description = "Personal exercise found")
    @ApiResponse(responseCode = "404", description = "Personal exercise not found")
    public ResponseEntity<APIResponse<PersonalExerciseDto>> getPersonalExerciseById(@PathVariable UUID id) {
        PersonalExerciseDto personalExercise = personalExerciseService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Personal exercise retrieved successfully", personalExercise));
    }

    @GetMapping("/schedule/{scheduleId}")
    @Operation(summary = "Get exercises by schedule", description = "Retrieve all personal exercises for a specific schedule, ordered by exercise order")
    @ApiResponse(responseCode = "200", description = "Personal exercises retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
    public ResponseEntity<APIResponse<List<PersonalExerciseDto>>> getExercisesBySchedule(@PathVariable UUID scheduleId) {
        List<PersonalExerciseDto> exercises = personalExerciseService.getByPersonalScheduleId(scheduleId);
        return ResponseEntity.ok(APIResponse.success("Personal exercises retrieved successfully", exercises));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed exercises", description = "Retrieve all completed personal exercises")
    @ApiResponse(responseCode = "200", description = "Completed exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalExerciseDto>>> getCompletedExercises() {
        List<PersonalExerciseDto> exercises = personalExerciseService.getCompleted();
        return ResponseEntity.ok(APIResponse.success("Completed exercises retrieved successfully", exercises));
    }

    @GetMapping("/incomplete")
    @Operation(summary = "Get incomplete exercises", description = "Retrieve all incomplete personal exercises")
    @ApiResponse(responseCode = "200", description = "Incomplete exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalExerciseDto>>> getIncompleteExercises() {
        List<PersonalExerciseDto> exercises = personalExerciseService.getIncomplete();
        return ResponseEntity.ok(APIResponse.success("Incomplete exercises retrieved successfully", exercises));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update personal exercise", description = "Update an existing personal exercise")
    @ApiResponse(responseCode = "200", description = "Personal exercise updated successfully")
    @ApiResponse(responseCode = "404", description = "Personal exercise not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<PersonalExerciseDto>> updatePersonalExercise(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonalExerciseRequest request) {
        PersonalExerciseDto personalExercise = personalExerciseService.updatePersonalExercise(id, request);
        return ResponseEntity.ok(APIResponse.success("Personal exercise updated successfully", personalExercise));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark exercise as completed", description = "Mark a personal exercise as completed")
    @ApiResponse(responseCode = "200", description = "Exercise marked as completed")
    @ApiResponse(responseCode = "404", description = "Personal exercise not found")
    public ResponseEntity<APIResponse<PersonalExerciseDto>> markAsCompleted(@PathVariable UUID id) {
        PersonalExerciseDto personalExercise = personalExerciseService.markAsCompleted(id);
        return ResponseEntity.ok(APIResponse.success("Exercise marked as completed", personalExercise));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete personal exercise", description = "Permanently delete a personal exercise")
    @ApiResponse(responseCode = "200", description = "Personal exercise deleted successfully")
    @ApiResponse(responseCode = "404", description = "Personal exercise not found")
    public ResponseEntity<APIResponse<Void>> deletePersonalExercise(@PathVariable UUID id) {
        personalExerciseService.deletePersonalExercise(id);
        return ResponseEntity.ok(APIResponse.success("Personal exercise deleted successfully", null));
    }
}

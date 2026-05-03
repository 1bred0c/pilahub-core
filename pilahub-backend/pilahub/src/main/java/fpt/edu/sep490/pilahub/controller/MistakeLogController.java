package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.MistakeLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchMistakeLogsRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.MistakeLogService;
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
@RequestMapping("/api/mistake-logs")
@RequiredArgsConstructor
@Tag(name = "Mistake Log", description = "Mistake log endpoints for trainees")
public class MistakeLogController {

    private final MistakeLogService mistakeLogService;

    @PostMapping("/batch")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Batch create mistake logs", description = "Create multiple mistake logs for a workout session")
    @ApiResponse(responseCode = "201", description = "Mistake logs created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<List<MistakeLogDto>>> batchCreateMistakeLogs(
            @Valid @RequestBody BatchMistakeLogsRequest request) {
        List<MistakeLogDto> logs = mistakeLogService.batchCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Mistake logs created successfully", logs));
    }

    @GetMapping("/workout-session/{workoutSessionId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get mistake logs by workout session", description = "Retrieve all mistake logs for a specific workout session")
    @ApiResponse(responseCode = "200", description = "Mistake logs retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<List<MistakeLogDto>>> getMistakeLogsByWorkoutSession(
            @PathVariable UUID workoutSessionId) {
        List<MistakeLogDto> logs = mistakeLogService.getByWorkoutSessionId(workoutSessionId);
        return ResponseEntity.ok(APIResponse.success("Mistake logs retrieved successfully", logs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get mistake log by ID", description = "Retrieve a specific mistake log by its ID")
    @ApiResponse(responseCode = "200", description = "Mistake log found")
    @ApiResponse(responseCode = "404", description = "Mistake log not found")
    public ResponseEntity<APIResponse<MistakeLogDto>> getMistakeLogById(@PathVariable UUID id) {
        MistakeLogDto log = mistakeLogService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Mistake log retrieved successfully", log));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete mistake log", description = "Permanently delete a mistake log")
    @ApiResponse(responseCode = "200", description = "Mistake log deleted successfully")
    @ApiResponse(responseCode = "404", description = "Mistake log not found")
    public ResponseEntity<APIResponse<Void>> deleteMistakeLog(@PathVariable UUID id) {
        mistakeLogService.deleteById(id);
        return ResponseEntity.ok(APIResponse.success("Mistake log deleted successfully", null));
    }
}


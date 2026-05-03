package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.HeartRateLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchHeartRateLogsRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.HeartRateLogService;
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
@RequestMapping("/api/heart-rate-logs")
@RequiredArgsConstructor
@Tag(name = "Heart Rate Log", description = "Heart rate log endpoints for trainees")
public class HeartRateLogController {

    private final HeartRateLogService heartRateLogService;

    @PostMapping("/batch")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Batch create heart rate logs", description = "Create multiple heart rate logs for a workout session")
    @ApiResponse(responseCode = "201", description = "Heart rate logs created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<List<HeartRateLogDto>>> batchCreateHeartRateLogs(
            @Valid @RequestBody BatchHeartRateLogsRequest request) {
        List<HeartRateLogDto> logs = heartRateLogService.batchCreate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Heart rate logs created successfully", logs));
    }

    @GetMapping("/workout-session/{workoutSessionId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get heart rate logs by workout session", description = "Retrieve all heart rate logs for a specific workout session")
    @ApiResponse(responseCode = "200", description = "Heart rate logs retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Workout session not found")
    public ResponseEntity<APIResponse<List<HeartRateLogDto>>> getHeartRateLogsByWorkoutSession(
            @PathVariable UUID workoutSessionId) {
        List<HeartRateLogDto> logs = heartRateLogService.getByWorkoutSessionId(workoutSessionId);
        return ResponseEntity.ok(APIResponse.success("Heart rate logs retrieved successfully", logs));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Get heart rate log by ID", description = "Retrieve a specific heart rate log by its ID")
    @ApiResponse(responseCode = "200", description = "Heart rate log found")
    @ApiResponse(responseCode = "404", description = "Heart rate log not found")
    public ResponseEntity<APIResponse<HeartRateLogDto>> getHeartRateLogById(@PathVariable UUID id) {
        HeartRateLogDto log = heartRateLogService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Heart rate log retrieved successfully", log));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(summary = "Delete heart rate log", description = "Permanently delete a heart rate log")
    @ApiResponse(responseCode = "200", description = "Heart rate log deleted successfully")
    @ApiResponse(responseCode = "404", description = "Heart rate log not found")
    public ResponseEntity<APIResponse<Void>> deleteHeartRateLog(@PathVariable UUID id) {
        heartRateLogService.deleteById(id);
        return ResponseEntity.ok(APIResponse.success("Heart rate log deleted successfully", null));
    }
}


package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.CoachBusyScheduleDto;
import fpt.edu.sep490.pilahub.dto.CoachTimeOffDto;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateCoachTimeOffRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.CoachTimeOffService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/coach-time-offs")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Coach Time Off", description = "Manage coach time off schedules")
public class CoachTimeOffController {

    private final CoachTimeOffService coachTimeOffService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Create time off", description = "Coach creates a time off period (max 8 hours per week, must be 24 hours in advance)")
    @ApiResponse(responseCode = "201", description = "Time off created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input or exceeds weekly limit")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden - Must have COACH role")
    public ResponseEntity<APIResponse<CoachTimeOffDto>> createTimeOff(
            @Valid @RequestBody CreateCoachTimeOffRequest request) {
        log.info("🎯 === CREATE TIME OFF ENDPOINT REACHED ===");
        log.info("Request: startTime={}, endTime={}, reason={}",
                request.startTime(), request.endTime(), request.reason());

        UUID coachId = securityUtil.getCurrentUserId();
        log.info("Coach ID from SecurityUtil: {}", coachId);
        log.info("Coach Role: {}", securityUtil.getCurrentUserRole());

        CoachTimeOffDto timeOff = coachTimeOffService.createTimeOff(coachId, request);
        log.info("✅ Time off created successfully with ID: {}", timeOff.id());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Time off created successfully", timeOff));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(summary = "Get time off by ID", description = "Retrieve a time off by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    @ApiResponse(responseCode = "404", description = "Time off not found")
    public ResponseEntity<APIResponse<CoachTimeOffDto>> getTimeOffById(@PathVariable("id") UUID timeOffId) {
        CoachTimeOffDto timeOff = coachTimeOffService.getTimeOffById(timeOffId);
        return ResponseEntity.ok(APIResponse.success("Time off retrieved successfully", timeOff));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get time offs for a coach", description = "Retrieve all time offs for a specific coach")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachTimeOffDto>>> getTimeOffsByCoach(
            @PathVariable("coachId") UUID coachId) {
        List<CoachTimeOffDto> timeOffs = coachTimeOffService.getTimeOffsByCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Time offs retrieved successfully", timeOffs));
    }

    @GetMapping("/coach/{coachId}/time-range")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(summary = "Get coach time offs in time range", description = "Retrieve time offs for a coach within a time range")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachTimeOffDto>>> getTimeOffsByCoachAndTimeRange(
            @PathVariable("coachId") UUID coachId,
            @RequestParam("startTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam("endTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        List<CoachTimeOffDto> timeOffs = coachTimeOffService.getTimeOffsByCoachAndTimeRange(coachId, startTime, endTime);
        return ResponseEntity.ok(APIResponse.success("Time offs retrieved successfully", timeOffs));
    }

    @GetMapping("/my-time-offs")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Get my time offs", description = "Coach retrieves their own time offs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachTimeOffDto>>> getMyTimeOffs() {
        log.info("🎯 === GET MY TIME OFFS ENDPOINT REACHED ===");
        UUID coachId = securityUtil.getCurrentUserId();
        log.info("Coach ID: {}, Role: {}", coachId, securityUtil.getCurrentUserRole());
        List<CoachTimeOffDto> timeOffs = coachTimeOffService.getTimeOffsByCoach(coachId);
        return ResponseEntity.ok(APIResponse.success("Your time offs retrieved successfully", timeOffs));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('COACH')")
    @Operation(summary = "Delete time off", description = "Delete a time off (must be at least 24 hours before it starts)")
    @ApiResponse(responseCode = "200", description = "Time off deleted successfully")
    @ApiResponse(responseCode = "400", description = "Cannot delete time off")
    @ApiResponse(responseCode = "404", description = "Time off not found")
    public ResponseEntity<APIResponse<Void>> deleteTimeOff(@PathVariable("id") UUID timeOffId) {
        UUID coachId = securityUtil.getCurrentUserId();
        coachTimeOffService.deleteTimeOff(timeOffId, coachId);
        return ResponseEntity.ok(APIResponse.success("Time off deleted successfully", null));
    }

    @GetMapping("/coach/{coachId}/busy-schedule")
    @PreAuthorize("hasAnyRole('COACH', 'TRAINEE', 'ADMIN')")
    @Operation(
            summary = "Get coach's total busy schedule",
            description = "Get coach's complete busy schedule including both time offs and active bookings. " +
                    "Excludes cancelled bookings (by coach/trainee) and refunded bookings. " +
                    "Optional time range filter - if not provided, returns all schedules."
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved busy schedule")
    @ApiResponse(responseCode = "404", description = "Coach not found")
    public ResponseEntity<APIResponse<List<CoachBusyScheduleDto>>> getCoachBusySchedule(
            @PathVariable("coachId") UUID coachId,
            @RequestParam(value = "startTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startTime,
            @RequestParam(value = "endTime", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endTime) {
        log.info("🎯 === GET COACH BUSY SCHEDULE ENDPOINT REACHED ===");
        log.info("Coach ID: {}, startTime: {}, endTime: {}", coachId, startTime, endTime);

        List<CoachBusyScheduleDto> busySchedule = coachTimeOffService.getCoachBusySchedule(coachId, startTime, endTime);
        log.info("✅ Retrieved {} busy schedule items", busySchedule.size());

        return ResponseEntity.ok(APIResponse.success("Coach busy schedule retrieved successfully", busySchedule));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all time offs (Admin)", description = "Admin retrieves all time offs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved")
    public ResponseEntity<APIResponse<List<CoachTimeOffDto>>> getAllTimeOffs() {
        List<CoachTimeOffDto> timeOffs = coachTimeOffService.getAllTimeOffs();
        return ResponseEntity.ok(APIResponse.success("All time offs retrieved successfully", timeOffs));
    }
}


package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalScheduleRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.PersonalScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/personal-schedules")
@RequiredArgsConstructor
@Tag(name = "Personal Schedule", description = "Personal schedule management endpoints")
public class PersonalScheduleController {

    private final PersonalScheduleService personalScheduleService;

    @PostMapping
    @Operation(summary = "Create personal schedule", description = "Create a new personal schedule for a stage")
    @ApiResponse(responseCode = "201", description = "Personal schedule created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Personal stage not found")
    public ResponseEntity<APIResponse<PersonalScheduleDto>> createPersonalSchedule(@Valid @RequestBody CreatePersonalScheduleRequest request) {
        PersonalScheduleDto schedule = personalScheduleService.createSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Personal schedule created successfully", schedule));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get personal schedule by ID", description = "Retrieve a specific personal schedule by its ID")
    @ApiResponse(responseCode = "200", description = "Personal schedule found")
    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
    public ResponseEntity<APIResponse<PersonalScheduleDto>> getPersonalScheduleById(@PathVariable UUID id) {
        PersonalScheduleDto schedule = personalScheduleService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Personal schedule retrieved successfully", schedule));
    }

    @GetMapping("/stage/{stageId}")
    @Operation(summary = "Get schedules by stage", description = "Retrieve all personal schedules for a specific stage, ordered by scheduled date")
    @ApiResponse(responseCode = "200", description = "Personal schedules retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Personal stage not found")
    public ResponseEntity<APIResponse<List<PersonalScheduleDto>>> getSchedulesByStage(@PathVariable UUID stageId) {
        List<PersonalScheduleDto> schedules = personalScheduleService.getByPersonalStageId(stageId);
        return ResponseEntity.ok(APIResponse.success("Personal schedules retrieved successfully", schedules));
    }

    @GetMapping("/completed")
    @Operation(summary = "Get completed schedules", description = "Retrieve all completed personal schedules")
    @ApiResponse(responseCode = "200", description = "Completed schedules retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalScheduleDto>>> getCompletedSchedules() {
        List<PersonalScheduleDto> schedules = personalScheduleService.getCompleted();
        return ResponseEntity.ok(APIResponse.success("Completed schedules retrieved successfully", schedules));
    }

    @GetMapping("/incomplete")
    @Operation(summary = "Get incomplete schedules", description = "Retrieve all incomplete personal schedules")
    @ApiResponse(responseCode = "200", description = "Incomplete schedules retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalScheduleDto>>> getIncompleteSchedules() {
        List<PersonalScheduleDto> schedules = personalScheduleService.getIncomplete();
        return ResponseEntity.ok(APIResponse.success("Incomplete schedules retrieved successfully", schedules));
    }

    @GetMapping("/date-range")
    @Operation(summary = "Get schedules by date range", description = "Retrieve personal schedules within a specific date range")
    @ApiResponse(responseCode = "200", description = "Schedules retrieved successfully")
    public ResponseEntity<APIResponse<List<PersonalScheduleDto>>> getSchedulesByDateRange(
            @RequestParam Instant startDate,
            @RequestParam Instant endDate) {
        List<PersonalScheduleDto> schedules = personalScheduleService.getByDateRange(startDate, endDate);
        return ResponseEntity.ok(APIResponse.success("Schedules retrieved successfully", schedules));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update personal schedule", description = "Update an existing personal schedule")
    @ApiResponse(responseCode = "200", description = "Personal schedule updated successfully")
    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<PersonalScheduleDto>> updatePersonalSchedule(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePersonalScheduleRequest request) {
        PersonalScheduleDto schedule = personalScheduleService.updateSchedule(id, request);
        return ResponseEntity.ok(APIResponse.success("Personal schedule updated successfully", schedule));
    }

    @PatchMapping("/{id}/complete")
    @Operation(summary = "Mark schedule as completed", description = "Mark a personal schedule as completed")
    @ApiResponse(responseCode = "200", description = "Schedule marked as completed")
    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
    public ResponseEntity<APIResponse<PersonalScheduleDto>> markAsCompleted(@PathVariable UUID id) {
        PersonalScheduleDto schedule = personalScheduleService.markAsCompleted(id);
        return ResponseEntity.ok(APIResponse.success("Schedule marked as completed", schedule));
    }
//
//    @PatchMapping("/{id}/incomplete")
//    @Operation(summary = "Mark schedule as incomplete", description = "Mark a personal schedule as incomplete")
//    @ApiResponse(responseCode = "200", description = "Schedule marked as incomplete")
//    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
//    public ResponseEntity<APIResponse<PersonalScheduleDto>> markAsIncomplete(@PathVariable UUID id) {
//        PersonalScheduleDto schedule = personalScheduleService.markAsIncomplete(id);
//        return ResponseEntity.ok(APIResponse.success("Schedule marked as incomplete", schedule));
//    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete personal schedule", description = "Permanently delete a personal schedule")
    @ApiResponse(responseCode = "200", description = "Personal schedule deleted successfully")
    @ApiResponse(responseCode = "404", description = "Personal schedule not found")
    public ResponseEntity<APIResponse<Void>> deletePersonalSchedule(@PathVariable UUID id) {
        personalScheduleService.deleteSchedule(id);
        return ResponseEntity.ok(APIResponse.success("Personal schedule deleted successfully", null));
    }
}

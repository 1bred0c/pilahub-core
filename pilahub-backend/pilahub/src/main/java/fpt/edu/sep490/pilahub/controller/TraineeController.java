package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TraineeDto;
import fpt.edu.sep490.pilahub.dto.request.CreateTraineeRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateTraineeRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.DailyTaskResponse;
import fpt.edu.sep490.pilahub.service.DailyTaskService;
import fpt.edu.sep490.pilahub.service.TraineeService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainees")
@RequiredArgsConstructor
@Tag(name = "Trainee Management", description = "APIs for managing trainee profiles")
public class TraineeController {

        private final TraineeService traineeService;
        private final DailyTaskService dailyTaskService;
        private final SecurityUtil securityUtil;

        // ==================== TRAINEE ENDPOINTS (using own token) ====================

        @PostMapping("/profile")
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Create trainee profile (Trainee only)", description = "Create trainee profile for the authenticated trainee. Uses account ID from JWT token.")
        @ApiResponse(responseCode = "201", description = "Trainee profile created successfully")
        @ApiResponse(responseCode = "400", description = "Invalid input or profile already exists")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<TraineeDto>> createOwnProfile(
                        @Valid @RequestBody CreateTraineeRequest request) {
                UUID accountId = securityUtil.getCurrentUserId();
                TraineeDto traineeDto = traineeService.createTrainee(accountId, request);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(APIResponse.success("Trainee profile created successfully", traineeDto));
        }

        @GetMapping("/profile")
        // @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Get own trainee profile (Trainee only)", description = "Retrieve trainee profile for the authenticated trainee. Uses account ID from JWT token.")
        @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
        @ApiResponse(responseCode = "404", description = "Profile not found")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<TraineeDto>> getOwnProfile() {
                UUID accountId = securityUtil.getCurrentUserId();
                TraineeDto traineeDto = traineeService.getTraineeByAccountId(accountId);
                return ResponseEntity.ok(APIResponse.success("Profile retrieved successfully", traineeDto));
        }

        @PutMapping("/profile")
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Update own trainee profile (Trainee only)", description = "Update trainee profile for the authenticated trainee. Uses account ID from JWT token.")
        @ApiResponse(responseCode = "200", description = "Profile updated successfully")
        @ApiResponse(responseCode = "404", description = "Profile not found")
        @ApiResponse(responseCode = "400", description = "Invalid input")
        @ApiResponse(responseCode = "401", description = "Unauthorized")
        public ResponseEntity<APIResponse<TraineeDto>> updateOwnProfile(
                        @Valid @RequestBody UpdateTraineeRequest request) {
                UUID accountId = securityUtil.getCurrentUserId();
                TraineeDto traineeDto = traineeService.updateTrainee(accountId, request);
                return ResponseEntity.ok(APIResponse.success("Profile updated successfully", traineeDto));
        }

        // ==================== ADMIN ENDPOINTS ====================

        @GetMapping
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Get all trainees (Admin only)", description = "Retrieve a list of all trainee profiles. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Trainees retrieved successfully")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<List<TraineeDto>>> getAllTrainees() {
                List<TraineeDto> trainees = traineeService.getAllTrainees();
                return ResponseEntity.ok(APIResponse.success(
                                String.format("Retrieved %d trainee(s) successfully", trainees.size()),
                                trainees));
        }

        @GetMapping("/{traineeId}")
        @Operation(summary = "Get trainee by ID (Admin only)", description = "Retrieve trainee profile by trainee ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Trainee found")
        @ApiResponse(responseCode = "404", description = "Trainee not found")
        public ResponseEntity<APIResponse<TraineeDto>> getTraineeById(
                        @PathVariable UUID traineeId) {
                TraineeDto traineeDto = traineeService.getTraineeById(traineeId);
                return ResponseEntity.ok(APIResponse.success("Trainee retrieved successfully", traineeDto));
        }

        @PutMapping("/{traineeId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Update trainee by ID (Admin only)", description = "Update trainee profile by trainee ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Trainee updated successfully")
        @ApiResponse(responseCode = "404", description = "Trainee not found")
        @ApiResponse(responseCode = "400", description = "Invalid input")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<TraineeDto>> updateTraineeByAdmin(
                        @PathVariable UUID traineeId,
                        @Valid @RequestBody UpdateTraineeRequest request) {
                TraineeDto traineeDto = traineeService.updateTraineeByAdmin(traineeId, request);
                return ResponseEntity.ok(APIResponse.success("Trainee updated successfully", traineeDto));
        }

        @DeleteMapping("/{traineeId}")
        @PreAuthorize("hasRole('ADMIN')")
        @Operation(summary = "Delete trainee by ID (Admin only)", description = "Delete trainee profile by trainee ID. Admin access required.")
        @ApiResponse(responseCode = "200", description = "Trainee deleted successfully")
        @ApiResponse(responseCode = "404", description = "Trainee not found")
        @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
        public ResponseEntity<APIResponse<Void>> deleteTrainee(
                        @PathVariable UUID traineeId) {
                traineeService.deleteTrainee(traineeId);
                return ResponseEntity.ok(APIResponse.success("Trainee deleted successfully", null));
        }

        @GetMapping("/daily-tasks")
        @PreAuthorize("hasRole('TRAINEE')")
        @Operation(summary = "Get daily tasks", description = "Get the authenticated trainee's daily tasks, including bookings, roadmap schedules, and course schedules. If date is omitted, today is used.")
        @ApiResponse(responseCode = "200", description = "Daily tasks retrieved successfully")
        public ResponseEntity<APIResponse<DailyTaskResponse>> getDailyTasks(
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
                UUID traineeId = securityUtil.getCurrentUserId();
                DailyTaskResponse dailyTasks = dailyTaskService.getDailyTasks(traineeId, date);
                return ResponseEntity.ok(APIResponse.success("Daily tasks retrieved successfully", dailyTasks));
        }
}

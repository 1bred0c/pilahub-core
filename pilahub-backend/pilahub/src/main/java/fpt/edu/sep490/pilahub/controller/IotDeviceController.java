package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.IotDeviceDto;
import fpt.edu.sep490.pilahub.dto.request.CreateIotDeviceRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateIotDeviceRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.IotDeviceService;
import fpt.edu.sep490.pilahub.service.TraineeService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/api/iot-devices")
@RequiredArgsConstructor
@Tag(name = "IoT Device Management", description = "APIs for managing IoT devices")
public class IotDeviceController {

    private final IotDeviceService iotDeviceService;
    private final TraineeService traineeService;
    private final SecurityUtil securityUtil;

    // ==================== TRAINEE ENDPOINTS (using own token) ====================

    @PostMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Create IoT device (Trainee only)",
            description = "Create a new IoT device for the authenticated trainee. Uses account ID from JWT token to get trainee."
    )
    @ApiResponse(responseCode = "201", description = "IoT device created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Device identifier already exists")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<IotDeviceDto>> createIotDevice(
            @Valid @RequestBody CreateIotDeviceRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        UUID traineeId = traineeService.getTraineeByAccountId(accountId).traineeId();

        IotDeviceDto iotDeviceDto = iotDeviceService.createIotDevice(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("IoT device created successfully", iotDeviceDto));
    }

    @GetMapping
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get own IoT devices (Trainee only)",
            description = "Retrieve all IoT devices for the authenticated trainee. Uses account ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "IoT devices retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<List<IotDeviceDto>>> getOwnIotDevices() {
        UUID accountId = securityUtil.getCurrentUserId();
        UUID traineeId = traineeService.getTraineeByAccountId(accountId).traineeId();

        List<IotDeviceDto> devices = iotDeviceService.getIotDevicesByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d IoT device(s) successfully", devices.size()),
                devices
        ));
    }

    @GetMapping("/{iotDeviceId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get IoT device by ID (Trainee only)",
            description = "Retrieve a specific IoT device by ID for the authenticated trainee."
    )
    @ApiResponse(responseCode = "200", description = "IoT device retrieved successfully")
    @ApiResponse(responseCode = "404", description = "IoT device not found or does not belong to you")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<IotDeviceDto>> getOwnIotDeviceById(
            @PathVariable UUID iotDeviceId) {
        UUID accountId = securityUtil.getCurrentUserId();
        UUID traineeId = traineeService.getTraineeByAccountId(accountId).traineeId();

        IotDeviceDto iotDeviceDto = iotDeviceService.getIotDeviceById(traineeId, iotDeviceId);
        return ResponseEntity.ok(APIResponse.success("IoT device retrieved successfully", iotDeviceDto));
    }

    @PutMapping("/{iotDeviceId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Update IoT device (Trainee only)",
            description = "Update a specific IoT device for the authenticated trainee."
    )
    @ApiResponse(responseCode = "200", description = "IoT device updated successfully")
    @ApiResponse(responseCode = "404", description = "IoT device not found or does not belong to you")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<IotDeviceDto>> updateOwnIotDevice(
            @PathVariable UUID iotDeviceId,
            @Valid @RequestBody UpdateIotDeviceRequest request) {
        UUID accountId = securityUtil.getCurrentUserId();
        UUID traineeId = traineeService.getTraineeByAccountId(accountId).traineeId();

        IotDeviceDto iotDeviceDto = iotDeviceService.updateIotDevice(traineeId, iotDeviceId, request);
        return ResponseEntity.ok(APIResponse.success("IoT device updated successfully", iotDeviceDto));
    }

    @DeleteMapping("/{iotDeviceId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Delete IoT device (Trainee only)",
            description = "Delete a specific IoT device for the authenticated trainee."
    )
    @ApiResponse(responseCode = "200", description = "IoT device deleted successfully")
    @ApiResponse(responseCode = "404", description = "IoT device not found or does not belong to you")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    public ResponseEntity<APIResponse<Void>> deleteOwnIotDevice(
            @PathVariable UUID iotDeviceId) {
        UUID accountId = securityUtil.getCurrentUserId();
        UUID traineeId = traineeService.getTraineeByAccountId(accountId).traineeId();

        iotDeviceService.deleteIotDevice(traineeId, iotDeviceId);
        return ResponseEntity.ok(APIResponse.success("IoT device deleted successfully", null));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all IoT devices (Admin only)",
            description = "Retrieve a list of all IoT devices across all trainees. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "IoT devices retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<IotDeviceDto>>> getAllIotDevices() {
        List<IotDeviceDto> devices = iotDeviceService.getAllIotDevices();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d IoT device(s) successfully", devices.size()),
                devices
        ));
    }

    @GetMapping("/admin/{iotDeviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get IoT device by ID (Admin only)",
            description = "Retrieve any IoT device by ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "IoT device retrieved successfully")
    @ApiResponse(responseCode = "404", description = "IoT device not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<IotDeviceDto>> getIotDeviceByIdAdmin(
            @PathVariable UUID iotDeviceId) {
        IotDeviceDto iotDeviceDto = iotDeviceService.getIotDeviceByIdAdmin(iotDeviceId);
        return ResponseEntity.ok(APIResponse.success("IoT device retrieved successfully", iotDeviceDto));
    }

    @DeleteMapping("/admin/{iotDeviceId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete IoT device by ID (Admin only)",
            description = "Delete any IoT device by ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "IoT device deleted successfully")
    @ApiResponse(responseCode = "404", description = "IoT device not found")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<Void>> deleteIotDeviceAdmin(
            @PathVariable UUID iotDeviceId) {
        iotDeviceService.deleteIotDeviceAdmin(iotDeviceId);
        return ResponseEntity.ok(APIResponse.success("IoT device deleted successfully", null));
    }
}

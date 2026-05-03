package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.SystemConfigDto;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.CreateSystemConfigRequest;
import fpt.edu.sep490.pilahub.dto.request.systemconfig.UpdateSystemConfigRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
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
@RequestMapping("/api/system-configs")
@RequiredArgsConstructor
@Tag(name = "System Config", description = "APIs for managing system configuration")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @PostMapping
    @Operation(summary = "Create system config", description = "Create a new system config record (Admin only)")
    @ApiResponse(responseCode = "201", description = "System config created successfully")
    public ResponseEntity<APIResponse<SystemConfigDto>> create(
            @Valid @RequestBody CreateSystemConfigRequest request) {
        SystemConfigDto dto = systemConfigService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("System config created successfully", dto));
    }

    @GetMapping
    @Operation(summary = "Get all system configs", description = "Get all system config records (Admin only)")
    @ApiResponse(responseCode = "200", description = "System configs retrieved successfully")
    public ResponseEntity<APIResponse<List<SystemConfigDto>>> getAll() {
        List<SystemConfigDto> configs = systemConfigService.getAll();
        return ResponseEntity.ok(APIResponse.success("System configs retrieved successfully", configs));
    }

    @GetMapping("/current")
    @Operation(summary = "Get current system config", description = "Get the latest system config record (Admin only)")
    @ApiResponse(responseCode = "200", description = "Current system config retrieved successfully")
    public ResponseEntity<APIResponse<SystemConfigDto>> getCurrent() {
        SystemConfigDto dto = systemConfigService.getCurrentConfig();
        return ResponseEntity.ok(APIResponse.success("Current system config retrieved successfully", dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get system config by ID", description = "Get a specific system config record by ID (Admin only)")
    @ApiResponse(responseCode = "200", description = "System config retrieved successfully")
    public ResponseEntity<APIResponse<SystemConfigDto>> getById(@PathVariable("id") UUID configId) {
        SystemConfigDto dto = systemConfigService.getById(configId);
        return ResponseEntity.ok(APIResponse.success("System config retrieved successfully", dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update system config", description = "Update a system config record (Admin only)")
    @ApiResponse(responseCode = "200", description = "System config updated successfully")
    public ResponseEntity<APIResponse<SystemConfigDto>> update(
            @PathVariable("id") UUID configId,
            @Valid @RequestBody UpdateSystemConfigRequest request) {
        SystemConfigDto dto = systemConfigService.update(configId, request);
        return ResponseEntity.ok(APIResponse.success("System config updated successfully", dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete system config", description = "Delete a system config record (Admin only)")
    @ApiResponse(responseCode = "200", description = "System config deleted successfully")
    public ResponseEntity<APIResponse<Void>> delete(@PathVariable("id") UUID configId) {
        systemConfigService.delete(configId);
        return ResponseEntity.ok(APIResponse.success("System config deleted successfully", null));
    }
}

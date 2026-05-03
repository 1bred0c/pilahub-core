package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import fpt.edu.sep490.pilahub.dto.request.CreateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.HealthProfileWithAssessmentResponse;
import fpt.edu.sep490.pilahub.service.HealthProfileService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/health-profiles")
@RequiredArgsConstructor
@Tag(name = "Health Profile Management", description = "APIs for managing trainee health profiles")
public class HealthProfileController {

    private final HealthProfileService healthProfileService;
    private final SecurityUtil securityUtil;

    // ==================== TRAINEE ENDPOINTS (using own token) ====================

    @PostMapping("/my-profiles")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Create health profile (Trainee only)",
            description = "Create a new health profile for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "201", description = "Health profile created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<HealthProfileDto>> createOwnHealthProfile(
            @Valid @RequestBody CreateHealthProfileRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileDto healthProfileDto = healthProfileService.createHealthProfile(traineeId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Health profile created successfully", healthProfileDto));
    }

    @PostMapping(value = "/my-profiles/inbody-extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Create health profile from InBody scan (Trainee only)",
            description = "Upload an InBody paper scan image, extract metrics via AI, then execute full health profile creation flow. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "201", description = "Health profile created from InBody scan successfully")
    @ApiResponse(responseCode = "400", description = "Invalid image payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    @ApiResponse(responseCode = "500", description = "AI extraction failed")
    public ResponseEntity<APIResponse<HealthProfileDto>> extractOwnInBodyScan(
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "rawScanId", required = false) String rawScanId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileDto profile = healthProfileService.extractInBodyScan(traineeId, image, rawScanId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Health profile created from InBody scan successfully", profile));
    }

    @GetMapping("/my-profiles")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get all own health profiles (Trainee only)",
            description = "Retrieve all health profiles for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<List<HealthProfileDto>>> getOwnHealthProfiles() {
        UUID traineeId = securityUtil.getCurrentUserId();
        List<HealthProfileDto> profiles = healthProfileService.getAllHealthProfilesByTraineeId(traineeId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d health profile(s) successfully", profiles.size()),
                profiles
        ));
    }

    @GetMapping("/my-profiles/latest")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get latest health profile (Trainee only)",
            description = "Retrieve the latest health profile for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Latest profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "No health profile found")
    public ResponseEntity<APIResponse<HealthProfileDto>> getOwnLatestHealthProfile() {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileDto profile = healthProfileService.getLatestHealthProfile(traineeId);
        return ResponseEntity.ok(APIResponse.success("Latest health profile retrieved successfully", profile));
    }

    @GetMapping("/my-profiles/{healthProfileId}")
    @Operation(
            summary = "Get specific health profile (Trainee only)",
            description = "Retrieve a specific health profile by ID for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Access denied - Profile does not belong to trainee")
    @ApiResponse(responseCode = "404", description = "Health profile not found")
    public ResponseEntity<APIResponse<HealthProfileDto>> getOwnHealthProfileById(
            @PathVariable UUID healthProfileId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileDto profile = healthProfileService.getHealthProfileById(healthProfileId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Health profile retrieved successfully", profile));
    }

    @PutMapping("/my-profiles/{healthProfileId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Update health profile (Trainee only)",
            description = "Update a specific health profile for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Profile updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Access denied - Profile does not belong to trainee")
    @ApiResponse(responseCode = "404", description = "Health profile not found")
    public ResponseEntity<APIResponse<HealthProfileDto>> updateOwnHealthProfile(
            @PathVariable UUID healthProfileId,
            @Valid @RequestBody UpdateHealthProfileRequest request) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HealthProfileDto profile = healthProfileService.updateHealthProfile(healthProfileId, traineeId, request);
        return ResponseEntity.ok(APIResponse.success("Health profile updated successfully", profile));
    }

    @DeleteMapping("/my-profiles/{healthProfileId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Delete health profile (Trainee only)",
            description = "Delete a specific health profile for the authenticated trainee. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Profile deleted successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Access denied - Profile does not belong to trainee")
    @ApiResponse(responseCode = "404", description = "Health profile not found")
    public ResponseEntity<APIResponse<Void>> deleteOwnHealthProfile(
            @PathVariable UUID healthProfileId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        healthProfileService.deleteHealthProfile(healthProfileId, traineeId);
        return ResponseEntity.ok(APIResponse.success("Health profile deleted successfully", null));
    }

    // ==================== ADMIN ENDPOINTS ====================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all health profiles (Admin only)",
            description = "Retrieve all health profiles in the system. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    public ResponseEntity<APIResponse<List<HealthProfileDto>>> getAllHealthProfilesAdmin() {
        List<HealthProfileDto> profiles = healthProfileService.getAllHealthProfilesAdmin();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d health profile(s) successfully", profiles.size()),
                profiles
        ));
    }

    @GetMapping("/trainee/{traineeId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(
            summary = "Get all health profiles by trainee ID (Admin only)",
            description = "Retrieve all health profiles for a specific trainee. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Profiles retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Trainee not found")
    public ResponseEntity<APIResponse<List<HealthProfileDto>>> getHealthProfilesByTraineeIdAdmin(
            @PathVariable UUID traineeId) {
        List<HealthProfileDto> profiles = healthProfileService.getAllHealthProfilesByTraineeIdAdmin(traineeId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d health profile(s) successfully", profiles.size()),
                profiles
        ));
    }

    @GetMapping("/{healthProfileId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get health profile by ID (Admin only)",
            description = "Retrieve a specific health profile by ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Profile retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Health profile not found")
    public ResponseEntity<APIResponse<HealthProfileDto>> getHealthProfileByIdAdmin(
            @PathVariable UUID healthProfileId) {
        HealthProfileDto profile = healthProfileService.getHealthProfileByIdAdmin(healthProfileId);
        return ResponseEntity.ok(APIResponse.success("Health profile retrieved successfully", profile));
    }

    @DeleteMapping("/{healthProfileId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Delete health profile by ID (Admin only)",
            description = "Delete a specific health profile by ID. Admin access required."
    )
    @ApiResponse(responseCode = "200", description = "Profile deleted successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin access required")
    @ApiResponse(responseCode = "404", description = "Health profile not found")
    public ResponseEntity<APIResponse<Void>> deleteHealthProfileAdmin(
            @PathVariable UUID healthProfileId) {
        healthProfileService.deleteHealthProfileAdmin(healthProfileId);
        return ResponseEntity.ok(APIResponse.success("Health profile deleted successfully", null));
    }

    @GetMapping("/trainee/{traineeId}/latest-with-assessment")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(
            summary = "Get latest health profile with assessment (Admin/Coach only)",
            description = "Retrieve the latest health profile along with its assessment for a specific trainee. Admin or Coach access required."
    )
    @ApiResponse(responseCode = "200", description = "Latest profile with assessment retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin or Coach access required")
    @ApiResponse(responseCode = "404", description = "Trainee not found or no health profile found")
    public ResponseEntity<APIResponse<HealthProfileWithAssessmentResponse>> getLatestHealthProfileWithAssessment(
            @PathVariable UUID traineeId) {
        HealthProfileWithAssessmentResponse response = healthProfileService.getLatestHealthProfileWithAssessmentAdmin(traineeId);
        return ResponseEntity.ok(APIResponse.success("Latest health profile with assessment retrieved successfully", response));
    }

    @GetMapping("/trainee/{traineeId}/metrics")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(
            summary = "Get health profile metrics for charts (Admin/Coach only)",
            description = "Retrieve all health profile metrics over time for a specific trainee. Returns data suitable for chart visualization including historical trends and latest comparison."
    )
    @ApiResponse(responseCode = "200", description = "Health profile metrics retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Forbidden - Admin or Coach access required")
    @ApiResponse(responseCode = "404", description = "Trainee not found or no health profiles found")
    public ResponseEntity<APIResponse<fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse>> getHealthProfileMetrics(
            @PathVariable UUID traineeId) {
        fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse response = healthProfileService.getHealthProfileMetrics(traineeId);
        return ResponseEntity.ok(APIResponse.success("Health profile metrics retrieved successfully", response));
    }

    @GetMapping("/my-profiles/metrics")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get own health profile metrics for charts (Trainee only)",
            description = "Retrieve all health profile metrics over time for the authenticated trainee. Returns data suitable for chart visualization including historical trends and latest comparison. Uses trainee ID from JWT token."
    )
    @ApiResponse(responseCode = "200", description = "Health profile metrics retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "No health profiles found")
    public ResponseEntity<APIResponse<fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse>> getOwnHealthProfileMetrics() {
        UUID traineeId = securityUtil.getCurrentUserId();
        fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse response = healthProfileService.getHealthProfileMetrics(traineeId);
        return ResponseEntity.ok(APIResponse.success("Health profile metrics retrieved successfully", response));
    }
}

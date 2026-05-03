package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import fpt.edu.sep490.pilahub.dto.request.CreateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateHealthProfileRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface HealthProfileService {

    /**
     * Create a new health profile for a trainee
     * @param traineeId ID of the trainee
     * @param request Health profile creation request
     * @return Created health profile DTO
     */
    HealthProfileDto createHealthProfile(UUID traineeId, CreateHealthProfileRequest request);

    /**
     * Get a specific health profile by ID
     * @param healthProfileId ID of the health profile
     * @param traineeId ID of the trainee (for authorization check)
     * @return Health profile DTO
     */
    HealthProfileDto getHealthProfileById(UUID healthProfileId, UUID traineeId);

    /**
     * Get all health profiles for a trainee
     * @param traineeId ID of the trainee
     * @return List of health profile DTOs
     */
    List<HealthProfileDto> getAllHealthProfilesByTraineeId(UUID traineeId);

    /**
     * Get the latest health profile for a trainee
     * @param traineeId ID of the trainee
     * @return Latest health profile DTO
     */
    HealthProfileDto getLatestHealthProfile(UUID traineeId);

    /**
     * Update a health profile
     * @param healthProfileId ID of the health profile
     * @param traineeId ID of the trainee (for authorization check)
     * @param request Health profile update request
     * @return Updated health profile DTO
     */
    HealthProfileDto updateHealthProfile(UUID healthProfileId, UUID traineeId, UpdateHealthProfileRequest request);

    /**
     * Delete a health profile
     * @param healthProfileId ID of the health profile
     * @param traineeId ID of the trainee (for authorization check)
     */
    void deleteHealthProfile(UUID healthProfileId, UUID traineeId);

    /**
     * Get a health profile by ID (Admin only - no trainee check)
     * @param healthProfileId ID of the health profile
     * @return Health profile DTO
     */
    HealthProfileDto getHealthProfileByIdAdmin(UUID healthProfileId);

    /**
     * Get all health profiles for a trainee (Admin only)
     * @param traineeId ID of the trainee
     * @return List of health profile DTOs
     */
    List<HealthProfileDto> getAllHealthProfilesByTraineeIdAdmin(UUID traineeId);

    /**
     * Get all health profiles in the system (Admin only)
     * @return List of all health profile DTOs
     */
    List<HealthProfileDto> getAllHealthProfilesAdmin();

    /**
     * Delete a health profile (Admin only - no trainee check)
     * @param healthProfileId ID of the health profile
     */
    void deleteHealthProfileAdmin(UUID healthProfileId);

    /**
     * Get latest health profile with its assessment for a trainee (Admin/Coach only)
     * @param traineeId ID of the trainee
     * @return Health profile with assessment response
     */
    fpt.edu.sep490.pilahub.dto.response.HealthProfileWithAssessmentResponse getLatestHealthProfileWithAssessmentAdmin(UUID traineeId);

    /**
     * Get health profile metrics for chart visualization
     * @param traineeId ID of the trainee
     * @return Health profile metrics response with historical data and comparison
     */
    fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse getHealthProfileMetrics(UUID traineeId);

    /**
     * Extract health profile metrics from an InBody scan image.
     * @param traineeId ID of authenticated trainee
     * @param image InBody scan image file
     * @param rawScanId optional raw scan identifier to send to AI server
     * @return created health profile after completing normal creation flow
     */
    HealthProfileDto extractInBodyScan(UUID traineeId, MultipartFile image, String rawScanId);
}

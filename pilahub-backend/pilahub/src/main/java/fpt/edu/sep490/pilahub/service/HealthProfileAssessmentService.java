package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;

import java.util.UUID;

public interface HealthProfileAssessmentService {

    /**
     * Create assessment by calling AI server
     * @param healthProfile The health profile to assess
     * @return Created assessment DTO
     */
    HealthProfileAssessmentDto createAssessment(HealthProfile healthProfile);

    /**
     * Get assessment by health profile ID
     * @param healthProfileId Health profile ID
     * @param traineeId Trainee ID for authorization
     * @return Assessment DTO
     */
    HealthProfileAssessmentDto getAssessmentByHealthProfileId(UUID healthProfileId, UUID traineeId);

    /**
     * Delete assessment by health profile ID
     * @param healthProfileId Health profile ID
     */
    void deleteAssessmentByHealthProfileId(UUID healthProfileId);
}

package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Health profile with its corresponding assessment")
public record HealthProfileWithAssessmentResponse(
        @Schema(description = "Health profile information")
        HealthProfileDto healthProfile,

        @Schema(description = "Health profile assessment (may be null if no assessment exists)")
        HealthProfileAssessmentDto assessment
) {
}


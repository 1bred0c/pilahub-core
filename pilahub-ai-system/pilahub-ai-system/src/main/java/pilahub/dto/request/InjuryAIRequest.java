package pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Injury information for AI request")
public record InjuryAIRequest(
        @Schema(description = "Injury name", example = "Lower Back Pain")
        String name,

        @Schema(description = "Injury description", example = "Chronic lower back pain from poor posture")
        String description,

        @Schema(description = "Injury symptoms", example = "Sharp pain when bending, stiffness in morning")
        String symptoms,

        @Schema(description = "Severity level", example = "MODERATE")
        String severity,

        @Schema(description = "Causes of the injury")
        String causes,

        @Schema(description = "Treatment suggestions")
        @JsonProperty("treatmentSuggestions")
        String treatmentSuggestions,

        @Schema(description = "Prevention tips")
        @JsonProperty("preventionTips")
        String preventionTips,

        @Schema(description = "Affected body parts")
        @JsonProperty("affectedBodyParts")
        List<AffectedBodyPartAIRequest> affectedBodyParts,

        @Schema(description = "Injury status", example = "ACTIVE")
        String status
) {
}

package fpt.edu.sep490.pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Injury information for AI assessment")
public record InjuryAIRequest(
        @Schema(description = "Injury name", example = "Viêm gân Achilles")
        String name,

        @Schema(description = "Injury description")
        String description,

        @Schema(description = "Symptoms")
        String symptoms,

        @Schema(description = "Causes")
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

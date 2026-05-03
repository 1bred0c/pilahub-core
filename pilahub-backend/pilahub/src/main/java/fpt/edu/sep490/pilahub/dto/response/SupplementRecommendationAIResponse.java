package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Supplement recommendation from AI")
public record SupplementRecommendationAIResponse(
        @Schema(description = "Supplement name", example = "Whey Protein")
        @JsonProperty("supplementName")
        String supplementName,

        @Schema(description = "Recommended timing", example = "Post-workout")
        @JsonProperty("recommendedTiming")
        String recommendedTiming,

        @Schema(description = "Dosage recommendation", example = "25-30g per serving")
        String dosage,

        @Schema(description = "Reason for recommendation", example = "Supports muscle recovery and growth")
        String reason,

        @Schema(description = "Priority level", example = "HIGH")
        String priority,

        @Schema(description = "Image URL of the supplement", example = "https://example.com/supplements/whey-protein.jpg")
        @JsonProperty("imageUrl")
        String imageUrl
) {
}

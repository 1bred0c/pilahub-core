package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import fpt.edu.sep490.pilahub.enums.AIModel;
import fpt.edu.sep490.pilahub.enums.HealthProfileLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Health profile assessment response from AI server")
public record HealthProfileAssessmentAIResponse(
        @Schema(description = "Assessment score (0-100)", example = "75")
        Integer score,

        @Schema(description = "Health profile level", example = "GOOD")
        @JsonProperty("healthProfileLevel")
        HealthProfileLevel healthProfileLevel,

        @Schema(description = "Assessment highlights (JSONB)")
        JsonNode highlights,

        @Schema(description = "Assessment risks (JSONB)")
        JsonNode risks,

        @Schema(description = "Assessment explanations (JSONB)")
        JsonNode explanations,

        @Schema(description = "Recommendations (JSONB)")
        JsonNode recommendations,

        @Schema(description = "Confidence score (0.0-1.0)", example = "0.85")
        @JsonProperty("confidenceScore")
        BigDecimal confidenceScore,

        @Schema(description = "AI model used", example = "GEMINI_3_FLASH_PREVIEW")
        @JsonProperty("aiModel")
        AIModel aiModel,

        @Schema(description = "Assessment timestamp", example = "2026-01-31T10:30:00Z")
        @JsonProperty("assessedAt")
        Instant assessedAt
) {
}

package fpt.edu.sep490.pilahub.dto;

import com.fasterxml.jackson.databind.JsonNode;
import fpt.edu.sep490.pilahub.enums.AIModel;
import fpt.edu.sep490.pilahub.enums.HealthProfileLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Health profile assessment information")
public record HealthProfileAssessmentDto(
        @Schema(description = "Unique assessment identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID healthProfileAssessmentId,

        @Schema(description = "Associated health profile ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID healthProfileId,

        @Schema(description = "Assessment score (0-100)", example = "75")
        Integer score,

        @Schema(description = "Health profile level", example = "GOOD")
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
        BigDecimal confidenceScore,

        @Schema(description = "AI model used", example = "GEMINI_3_FLASH_PREVIEW")
        AIModel aiModel,

        @Schema(description = "Creation timestamp", example = "2026-01-31T10:30:00Z")
        Instant createdAt
) {
}

package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.enums.ProfileSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "InBody extraction result that can be used to prefill health profile input")
public record InBodyExtractionResponse(
        @Schema(description = "Height in centimeters", example = "173.0")
        BigDecimal heightCm,

        @Schema(description = "Weight in kilograms", example = "98.9")
        BigDecimal weightKg,

        @Schema(description = "Body Mass Index", example = "33.0")
        BigDecimal bmi,

        @Schema(description = "Body fat percentage", example = "19.7")
        BigDecimal bodyFatPercentage,

        @Schema(description = "Muscle mass in kilograms", example = "46.3")
        BigDecimal muscleMassKg,

        @Schema(description = "Waist in centimeters (null by design for InBody scan)", example = "null")
        BigDecimal waistCm,

        @Schema(description = "Hip in centimeters (null by design for InBody scan)", example = "null")
        BigDecimal hipCm,

        @Schema(description = "Mapped profile source for saving health profile", example = "InBody")
        ProfileSource source,

        @Schema(description = "Additional metadata JSON string from AI system", example = "{\"device\":\"InBody 270\"}")
        String metadata,

        @Schema(description = "Message returned by AI system", example = "Extracted successfully from InBody 270")
        String extractionMessage
) {
}


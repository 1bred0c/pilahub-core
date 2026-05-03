package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Extracted health metrics from InBody scan")
public record InBodyExtractData(
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

        @Schema(description = "Waist in centimeters (always null for this flow)", example = "null")
        BigDecimal waistCm,

        @Schema(description = "Hip in centimeters (always null for this flow)", example = "null")
        BigDecimal hipCm,

        @Schema(description = "Source tag returned by AI system", example = "INBODY_SCAN")
        String source,

        @Schema(description = "Additional metadata JSON string", example = "{\"device\":\"InBody 270\"}")
        String metadata
) {
}


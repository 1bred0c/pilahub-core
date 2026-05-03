package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Comparison of the latest values against previous values")
public record MetricComparison(
        @Schema(description = "Change in weight (kg)", example = "-1.3")
        BigDecimal weightKg,

        @Schema(description = "Change in BMI", example = "-0.4")
        BigDecimal bmi,

        @Schema(description = "Change in body fat percentage", example = "-0.8")
        BigDecimal bodyFatPercentage,

        @Schema(description = "Change in muscle mass (kg)", example = "0.3")
        BigDecimal muscleMassKg,

        @Schema(description = "Change in waist (cm)", example = "-1.5")
        BigDecimal waistCm,

        @Schema(description = "Change in hip (cm)", example = "-0.6")
        BigDecimal hipCm
) {
}


package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.ProfileSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to create a new health profile")
public record CreateHealthProfileRequest(
        @Schema(
                description = "Height in centimeters",
                example = "175.5"
        )
        @DecimalMin(value = "0.0", inclusive = false, message = "Height must be greater than 0")
        @DecimalMax(value = "300.0", message = "Height must not exceed 300 cm")
        BigDecimal heightCm,

        @Schema(
                description = "Weight in kilograms",
                example = "70.5"
        )
        @DecimalMin(value = "0.0", inclusive = false, message = "Weight must be greater than 0")
        @DecimalMax(value = "500.0", message = "Weight must not exceed 500 kg")
        BigDecimal weightKg,

        @Schema(
                description = "Body Mass Index",
                example = "22.9"
        )
        @DecimalMin(value = "0.0", message = "BMI must not be negative")
        @DecimalMax(value = "100.0", message = "BMI must not exceed 100")
        BigDecimal bmi,

        @Schema(
                description = "Body fat percentage",
                example = "15.5"
        )
        @DecimalMin(value = "0.0", message = "Body fat percentage must not be negative")
        @DecimalMax(value = "100.0", message = "Body fat percentage must not exceed 100")
        BigDecimal bodyFatPercentage,

        @Schema(
                description = "Muscle mass in kilograms",
                example = "32.5"
        )
        @DecimalMin(value = "0.0", message = "Muscle mass must not be negative")
        BigDecimal muscleMassKg,

        @Schema(
                description = "Waist measurement in centimeters",
                example = "80.0"
        )
        @DecimalMin(value = "0.0", message = "Waist measurement must not be negative")
        BigDecimal waistCm,

        @Schema(
                description = "Hip measurement in centimeters",
                example = "95.0"
        )
        @DecimalMin(value = "0.0", message = "Hip measurement must not be negative")
        BigDecimal hipCm,

        @Schema(
                description = "Source of the profile data",
                example = "Manual",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Profile source must not be null")
        ProfileSource source,

        @Schema(
                description = "Additional metadata in JSON format",
                example = "{\"device\": \"InBody 270\"}"
        )
        String metadata
) {
}

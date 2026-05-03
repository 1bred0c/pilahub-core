package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.ProfileSource;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Health profile information")
public record HealthProfileDto(
        @Schema(description = "Unique health profile identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID healthProfileId,

        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Height in centimeters", example = "175.5")
        BigDecimal heightCm,

        @Schema(description = "Weight in kilograms", example = "70.5")
        BigDecimal weightKg,

        @Schema(description = "Body Mass Index", example = "22.9")
        BigDecimal bmi,

        @Schema(description = "Body fat percentage", example = "15.5")
        BigDecimal bodyFatPercentage,

        @Schema(description = "Muscle mass in kilograms", example = "32.5")
        BigDecimal muscleMassKg,

        @Schema(description = "Waist measurement in centimeters", example = "80.0")
        BigDecimal waistCm,

        @Schema(description = "Hip measurement in centimeters", example = "95.0")
        BigDecimal hipCm,

        @Schema(description = "Source of the profile data", example = "Manual")
        ProfileSource source,

        @Schema(description = "Additional metadata", example = "{\"device\": \"InBody 270\"}")
        String metadata,

        @Schema(description = "Whether this is the latest profile", example = "true")
        boolean isLatest,

        @Schema(description = "Profile creation timestamp")
        Instant createdAt
) {
}

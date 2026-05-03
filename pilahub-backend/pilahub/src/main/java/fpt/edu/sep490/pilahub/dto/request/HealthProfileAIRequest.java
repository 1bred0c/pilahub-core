package fpt.edu.sep490.pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.ProfileSource;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Schema(description = "Request to AI server for health profile assessment")
public record HealthProfileAIRequest(
        @Schema(description = "Trainee's age", example = "25", required = true)
        @NotNull
        @Min(10)
        @Max(120)
        Integer age,

        @Schema(description = "Trainee's gender", example = "MALE", required = true)
        @NotNull
        Gender gender,

        @Schema(description = "Workout level", example = "BEGINNER", required = true)
        @NotNull
        @JsonProperty("workoutLevel")
        WorkoutLevel workoutLevel,

        @Schema(description = "Workout frequency", example = "MODERATE", required = true)
        @NotNull
        @JsonProperty("workoutFrequency")
        WorkoutFrequency workoutFrequency,

        @Schema(description = "List of injuries")
        List<InjuryAIRequest> injuries,

        @Schema(description = "Height in centimeters", example = "175.0", required = true)
        @NotNull
        @DecimalMin("50.0")
        @DecimalMax("300.0")
        @JsonProperty("heightCm")
        BigDecimal heightCm,

        @Schema(description = "Weight in kilograms", example = "70.0", required = true)
        @NotNull
        @DecimalMin("20.0")
        @DecimalMax("500.0")
        @JsonProperty("weightKg")
        BigDecimal weightKg,

        @Schema(description = "Body Mass Index", example = "22.86")
        BigDecimal bmi,

        @Schema(description = "Body fat percentage", example = "18.0")
        @JsonProperty("bodyFatPercentage")
        BigDecimal bodyFatPercentage,

        @Schema(description = "Muscle mass in kilograms", example = "32.5")
        @JsonProperty("muscleMassKg")
        BigDecimal muscleMassKg,

        @Schema(description = "Waist circumference in centimeters", example = "82.0")
        @JsonProperty("waistCm")
        BigDecimal waistCm,

        @Schema(description = "Hip circumference in centimeters", example = "96.0")
        @JsonProperty("hipCm")
        BigDecimal hipCm,

        @Schema(description = "Data source", example = "GoogleFit", required = true)
        @NotNull
        ProfileSource source,

        @Schema(description = "Additional metadata")
        @JsonProperty("metaData")
        Map<String, Object> metaData
) {
}

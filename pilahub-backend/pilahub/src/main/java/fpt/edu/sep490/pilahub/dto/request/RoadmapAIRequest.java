package fpt.edu.sep490.pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.util.List;

@Schema(description = "Request to AI server for roadmap generation")
public record RoadmapAIRequest(
                @Schema(description = "Primary fitness goal", example = "Relieve back pain", required = true) @NotBlank @JsonProperty("primaryGoal") String primaryGoal,

                @Schema(description = "Secondary fitness goals", example = "[\"Strengthen core and abs\", \"Improve flexibility\"]") @JsonProperty("secondaryGoals") List<String> secondaryGoals,

                @Schema(description = "Trainee's age", example = "25", required = true) @NotNull @Min(10) @Max(120) Integer age,

                @Schema(description = "Trainee's gender", example = "MALE", required = true) @NotNull Gender gender,

                @Schema(description = "Workout level", example = "INTERMEDIATE", required = true) @NotNull @JsonProperty("workoutLevel") WorkoutLevel workoutLevel,

                @Schema(description = "Workout frequency", example = "MODERATE", required = true) @NotNull @JsonProperty("workoutFrequency") WorkoutFrequency workoutFrequency,

                @Schema(description = "Training days of week", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]", required = true) @NotEmpty @JsonProperty("trainingDays") List<String> trainingDays,

                @Schema(description = "Duration in weeks", example = "12", required = true) @NotNull @Min(1) @Max(52) @JsonProperty("durationWeeks") Integer durationWeeks,

                @Schema(description = "Height in centimeters", example = "175.0") @JsonProperty("heightCm") BigDecimal heightCm,

                @Schema(description = "Weight in kilograms", example = "70.0") @JsonProperty("weightKg") BigDecimal weightKg,

                @Schema(description = "Body Mass Index", example = "22.9") BigDecimal bmi,

                @Schema(description = "Body fat percentage", example = "15.5") @JsonProperty("bodyFatPercentage") BigDecimal bodyFatPercentage,

                @Schema(description = "Muscle mass in kilograms", example = "32.5") @JsonProperty("muscleMassKg") BigDecimal muscleMassKg,

                @Schema(description = "Waist measurement in centimeters", example = "80.0") @JsonProperty("waistCm") BigDecimal waistCm,

                @Schema(description = "Hip measurement in centimeters", example = "95.0") @JsonProperty("hipCm") BigDecimal hipCm,

                @Schema(description = "List of injuries") List<InjuryAIRequest> injuries,

                @Schema(description = "Available stages in the system") @JsonProperty("availableStages") List<String> availableStages,

                @Schema(description = "Available exercises in the system") @JsonProperty("availableExercises") List<ExerciseAIRequest> availableExercises,

                @Schema(description = "Available supplements in the system") @JsonProperty("availableSupplements") List<SupplementAIRequest> availableSupplements) {
}

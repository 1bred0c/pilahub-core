package pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import pilahub.enums.Gender;
import pilahub.enums.WorkoutFrequency;
import pilahub.enums.WorkoutLevel;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for workout feedback analysis")
public class WorkoutFeedbackAnalysisRequest {

    @Schema(description = "Name of the exercise", example = "Pilates Roll-Up", required = true)
    @NotBlank(message = "Exercise name is required")
    @JsonProperty("exerciseName")
    private String exerciseName;

    @Schema(description = "Type of exercise", example = "SPINAL_ARTICULATION", required = true)
    @NotBlank(message = "Exercise type is required")
    @JsonProperty("exerciseType")
    private String exerciseType;

    @Schema(description = "Difficulty level of the exercise", example = "INTERMEDIATE", required = true)
    @NotBlank(message = "Difficulty level is required")
    @JsonProperty("difficultyLevel")
    private String difficultyLevel;

    @Schema(description = "Target body parts", example = "[\"Core\", \"Spine\"]", required = true)
    @NotEmpty(message = "Target body parts are required")
    @JsonProperty("targetBodyParts")
    private List<String> targetBodyParts;

    @Schema(description = "Duration of workout in seconds", example = "180.5", required = true)
    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be positive")
    @JsonProperty("durationSeconds")
    private Double durationSeconds;

    @Schema(description = "Mistake summary", required = true)
    @Valid
    @NotNull(message = "Mistake summary is required")
    @JsonProperty("mistakeSummary")
    private MistakeSummary mistakeSummary;

    @Schema(description = "Heart rate summary (optional)")
    @Valid
    @JsonProperty("heartRateSummary")
    private HeartRateSummary heartRateSummary;

    @Schema(description = "User context information", required = true)
    @Valid
    @NotNull(message = "User context is required")
    @JsonProperty("userContext")
    private UserContext userContext;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MistakeSummary {
        @Schema(description = "Total number of mistakes", example = "5", required = true)
        @NotNull(message = "Total mistakes is required")
        @Min(0)
        @JsonProperty("totalMistakes")
        private Integer totalMistakes;

        @Schema(description = "Mistakes grouped by body part", required = true)
        @NotEmpty(message = "Mistakes by body part is required")
        @JsonProperty("mistakesByBodyPart")
        private List<MistakeByBodyPart> mistakesByBodyPart;

        @Schema(description = "Average time between mistakes in seconds")
        @JsonProperty("averageTimeBetweenMistakes")
        private Double averageTimeBetweenMistakes;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MistakeByBodyPart {
        @Schema(description = "Name of the body part", example = "Lower Back", required = true)
        @NotBlank(message = "Body part name is required")
        @JsonProperty("bodyPartName")
        private String bodyPartName;

        @Schema(description = "Number of mistakes", example = "3", required = true)
        @NotNull(message = "Mistake count is required")
        @Min(0)
        @JsonProperty("count")
        private Integer count;

        @Schema(description = "Details of mistakes with timestamps", required = true)
        @NotEmpty(message = "Mistake details are required")
        @JsonProperty("details")
        private List<String> details;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeartRateSummary {
        @Schema(description = "Average heart rate in bpm", example = "128", required = true)
        @NotNull(message = "Average heart rate is required")
        @Min(40)
        @Max(220)
        @JsonProperty("averageHeartRate")
        private Integer averageHeartRate;

        @Schema(description = "Maximum heart rate in bpm", example = "152", required = true)
        @NotNull(message = "Max heart rate is required")
        @Min(40)
        @Max(220)
        @JsonProperty("maxHeartRate")
        private Integer maxHeartRate;

        @Schema(description = "Minimum heart rate in bpm", example = "105", required = true)
        @NotNull(message = "Min heart rate is required")
        @Min(40)
        @Max(220)
        @JsonProperty("minHeartRate")
        private Integer minHeartRate;

        @Schema(description = "Total number of heart rate readings", example = "45", required = true)
        @NotNull(message = "Total readings is required")
        @Positive(message = "Total readings must be positive")
        @JsonProperty("totalReadings")
        private Integer totalReadings;

        @Schema(description = "Heart rate zone distribution", required = true)
        @Valid
        @NotNull(message = "Heart rate zones are required")
        @JsonProperty("zones")
        private HeartRateZones zones;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HeartRateZones {
        @Schema(description = "Percentage in rest zone", example = "5.2")
        @NotNull
        @Min(0)
        @Max(100)
        @JsonProperty("restZone")
        private Double restZone;

        @Schema(description = "Percentage in fat burn zone", example = "45.8")
        @NotNull
        @Min(0)
        @Max(100)
        @JsonProperty("fatBurnZone")
        private Double fatBurnZone;

        @Schema(description = "Percentage in cardio zone", example = "42.3")
        @NotNull
        @Min(0)
        @Max(100)
        @JsonProperty("cardioZone")
        private Double cardioZone;

        @Schema(description = "Percentage in peak zone", example = "6.7")
        @NotNull
        @Min(0)
        @Max(100)
        @JsonProperty("peakZone")
        private Double peakZone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContext {
        @Schema(description = "User's age", example = "28", required = true)
        @NotNull(message = "Age is required")
        @Min(10)
        @Max(120)
        @JsonProperty("age")
        private Integer age;

        @Schema(description = "User's gender", example = "FEMALE", required = true)
        @NotBlank(message = "Gender is required")
        @JsonProperty("gender")
        private String gender;

        @Schema(description = "User's workout level", example = "INTERMEDIATE", required = true)
        @NotBlank(message = "Workout level is required")
        @JsonProperty("workoutLevel")
        private String workoutLevel;

        @Schema(description = "User's workout frequency", example = "MODERATE", required = true)
        @NotBlank(message = "Workout frequency is required")
        @JsonProperty("workoutFrequency")
        private String workoutFrequency;

        @Schema(description = "User's BMI (optional)", example = "22.5")
        @JsonProperty("bmi")
        private Double bmi;

        @Schema(description = "Active injuries", example = "[\"Lower Back Strain\"]", required = true)
        @NotNull(message = "Active injuries list is required")
        @JsonProperty("activeInjuries")
        private List<String> activeInjuries;
    }
}


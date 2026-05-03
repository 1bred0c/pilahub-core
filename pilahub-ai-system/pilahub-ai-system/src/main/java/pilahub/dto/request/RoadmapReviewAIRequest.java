package pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for AI-powered roadmap review analysis")
public class RoadmapReviewAIRequest {

    @Schema(description = "Roadmap info", required = true)
    @Valid
    @NotNull(message = "Roadmap is required")
    @JsonProperty("roadmap")
    private RoadmapInfo roadmap;

    @Schema(description = "Initial health profile", required = true)
    @Valid
    @NotNull(message = "Initial health profile is required")
    @JsonProperty("initialHealthProfile")
    private HealthProfileSnapshot initialHealthProfile;

    @Schema(description = "Final health profile", required = true)
    @Valid
    @NotNull(message = "Final health profile is required")
    @JsonProperty("finalHealthProfile")
    private HealthProfileSnapshot finalHealthProfile;

    @Schema(description = "Trainee context", required = true)
    @Valid
    @NotNull(message = "Trainee context is required")
    @JsonProperty("traineeContext")
    private TraineeContext traineeContext;

    @Schema(description = "Execution summary", required = true)
    @Valid
    @NotNull(message = "Execution summary is required")
    @JsonProperty("executionSummary")
    private ExecutionSummary executionSummary;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RoadmapInfo {
        @Schema(description = "Roadmap ID", example = "9f264d71-9b96-4bb2-8b7f-f4f4ed0ccf88", required = true)
        @NotBlank(message = "Roadmap ID is required")
        @JsonProperty("roadmapId")
        private String roadmapId;

        @Schema(description = "Roadmap title", example = "12-week Fat Loss + Core Strength")
        @JsonProperty("title")
        private String title;

        @Schema(description = "Roadmap description")
        @JsonProperty("description")
        private String description;

        @Schema(description = "Start date (ISO-8601)")
        @JsonProperty("startDate")
        private Instant startDate;

        @Schema(description = "End date (ISO-8601)")
        @JsonProperty("endDate")
        private Instant endDate;

        @Schema(description = "Progress percent", example = "100")
        @Min(0)
        @Max(100)
        @JsonProperty("progressPercent")
        private Integer progressPercent;

        @Schema(description = "Roadmap status", example = "IN_PROGRESS")
        @JsonProperty("status")
        private String status;

        @Schema(description = "Roadmap source", example = "AI_GENERATED")
        @JsonProperty("source")
        private String source;

        @Schema(description = "Goals in roadmap")
        @Valid
        @JsonProperty("goals")
        private List<GoalInfo> goals;

        @Schema(description = "Initial health profile ID")
        @JsonProperty("initialHealthProfileId")
        private String initialHealthProfileId;

        @Schema(description = "Final health profile ID")
        @JsonProperty("finalHealthProfileId")
        private String finalHealthProfileId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GoalInfo {
        @Schema(description = "Goal ID", example = "e3432d60-4c3f-4e76-bdd7-ea12f2e6956d")
        @JsonProperty("goalId")
        private String goalId;

        @Schema(description = "Goal code", example = "FAT_LOSS")
        @JsonProperty("code")
        private String code;

        @Schema(description = "Goal name", example = "Giảm mỡ")
        @JsonProperty("name")
        private String name;

        @Schema(description = "Is primary goal", example = "true")
        @JsonProperty("isPrimary")
        private Boolean isPrimary;

        @Schema(description = "Goal order", example = "1")
        @JsonProperty("goalOrder")
        private Integer goalOrder;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HealthProfileSnapshot {
        @Schema(description = "Health profile ID", example = "08d9e67b-d0b1-4458-9f83-1f7fbe3ab4ec")
        @JsonProperty("healthProfileId")
        private String healthProfileId;

        @Schema(description = "Created time")
        @JsonProperty("createdAt")
        private Instant createdAt;

        @Schema(description = "Height in cm", example = "170.0")
        @JsonProperty("heightCm")
        private BigDecimal heightCm;

        @Schema(description = "Weight in kg", example = "78.5")
        @JsonProperty("weightKg")
        private BigDecimal weightKg;

        @Schema(description = "BMI", example = "27.2")
        @JsonProperty("bmi")
        private BigDecimal bmi;

        @Schema(description = "Body fat percentage", example = "29.4")
        @JsonProperty("bodyFatPercentage")
        private BigDecimal bodyFatPercentage;

        @Schema(description = "Muscle mass (kg)", example = "29.8")
        @JsonProperty("muscleMassKg")
        private BigDecimal muscleMassKg;

        @Schema(description = "Waist (cm)", example = "91.0")
        @JsonProperty("waistCm")
        private BigDecimal waistCm;

        @Schema(description = "Hip (cm)", example = "102.0")
        @JsonProperty("hipCm")
        private BigDecimal hipCm;

        @Schema(description = "Data source", example = "InBody")
        @JsonProperty("source")
        private String source;

        @Schema(description = "Raw metadata JSON string")
        @JsonProperty("metadata")
        private String metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TraineeContext {
        @Schema(description = "Age", example = "27")
        @Min(10)
        @Max(120)
        @JsonProperty("age")
        private Integer age;

        @Schema(description = "Gender", example = "MALE")
        @JsonProperty("gender")
        private String gender;

        @Schema(description = "Workout frequency", example = "4")
        @Min(0)
        @JsonProperty("workoutFrequency")
        private Integer workoutFrequency;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExecutionSummary {
        @Schema(description = "Total schedules", example = "48")
        @Min(0)
        @JsonProperty("totalSchedules")
        private Integer totalSchedules;

        @Schema(description = "Completed schedules", example = "43")
        @Min(0)
        @JsonProperty("completedSchedules")
        private Integer completedSchedules;

        @Schema(description = "Total exercises", example = "320")
        @Min(0)
        @JsonProperty("totalExercises")
        private Integer totalExercises;

        @Schema(description = "Completed exercises", example = "286")
        @Min(0)
        @JsonProperty("completedExercises")
        private Integer completedExercises;

        @Schema(description = "Completion rate", example = "0.8938")
        @Min(0)
        @Max(1)
        @JsonProperty("completionRate")
        private Double completionRate;
    }
}


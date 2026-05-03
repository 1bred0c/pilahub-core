package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Request to AI server for roadmap review analysis")
public record RoadmapReviewAIRequest(
        @Schema(description = "Roadmap information", requiredMode = Schema.RequiredMode.REQUIRED)
        RoadmapInfo roadmap,

        @Schema(description = "Initial health profile snapshot", requiredMode = Schema.RequiredMode.REQUIRED)
        HealthProfileSnapshot initialHealthProfile,

        @Schema(description = "Final health profile snapshot", requiredMode = Schema.RequiredMode.REQUIRED)
        HealthProfileSnapshot finalHealthProfile,

        @Schema(description = "Trainee context")
        TraineeContext traineeContext,

        @Schema(description = "Execution summary")
        ExecutionSummary executionSummary
) {
    @Schema(description = "Roadmap summary")
    public record RoadmapInfo(
            @Schema(description = "Roadmap ID", example = "9f264d71-9b96-4bb2-8b7f-f4f4ed0ccf88")
            String roadmapId,

            @Schema(description = "Title", example = "12-week Fat Loss + Core Strength")
            String title,

            @Schema(description = "Description")
            String description,

            @Schema(description = "Start date (ISO 8601)", example = "2026-01-10T00:00:00Z")
            String startDate,

            @Schema(description = "End date (ISO 8601)", example = "2026-04-10T00:00:00Z")
            String endDate,

            @Schema(description = "Progress percent", example = "100")
            Integer progressPercent,

            @Schema(description = "Status", example = "IN_PROGRESS")
            String status,

            @Schema(description = "Source", example = "AI_GENERATED")
            String source,

            @Schema(description = "Goals")
            List<GoalInfo> goals,

            @Schema(description = "Initial health profile ID")
            String initialHealthProfileId,

            @Schema(description = "Final health profile ID")
            String finalHealthProfileId
    ) {}

    @Schema(description = "Goal information")
    public record GoalInfo(
            @Schema(description = "Goal ID")
            String goalId,

            @Schema(description = "Goal code")
            String code,

            @Schema(description = "Goal name")
            String name,

            @Schema(description = "Is primary goal", example = "true")
            Boolean isPrimary,

            @Schema(description = "Goal order", example = "1")
            Integer goalOrder
    ) {}

    @Schema(description = "Health profile snapshot")
    public record HealthProfileSnapshot(
            @Schema(description = "Health profile ID")
            String healthProfileId,

            @Schema(description = "Created at (ISO 8601)")
            String createdAt,

            @Schema(description = "Height in cm")
            BigDecimal heightCm,

            @Schema(description = "Weight in kg")
            BigDecimal weightKg,

            @Schema(description = "BMI")
            BigDecimal bmi,

            @Schema(description = "Body fat percentage")
            BigDecimal bodyFatPercentage,

            @Schema(description = "Muscle mass in kg")
            BigDecimal muscleMassKg,

            @Schema(description = "Waist in cm")
            BigDecimal waistCm,

            @Schema(description = "Hip in cm")
            BigDecimal hipCm,

            @Schema(description = "Source", example = "InBody")
            String source,

            @Schema(description = "Metadata JSON string")
            String metadata
    ) {}

    @Schema(description = "Trainee context")
    public record TraineeContext(
            @Schema(description = "Age", example = "27")
            Integer age,

            @Schema(description = "Gender", example = "MALE")
            String gender,

            @Schema(description = "Workout frequency per week", example = "4")
            Integer workoutFrequency
    ) {}

    @Schema(description = "Execution summary")
    public record ExecutionSummary(
            @Schema(description = "Total schedules", example = "48")
            Integer totalSchedules,

            @Schema(description = "Completed schedules", example = "43")
            Integer completedSchedules,

            @Schema(description = "Total exercises", example = "320")
            Integer totalExercises,

            @Schema(description = "Completed exercises", example = "286")
            Integer completedExercises,

            @Schema(description = "Completion rate", example = "0.8938")
            Double completionRate
    ) {}
}


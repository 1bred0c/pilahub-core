package fpt.edu.sep490.pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import fpt.edu.sep490.pilahub.enums.AIModel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Roadmap generation response from AI server")
public record RoadmapAIResponse(
        @Schema(description = "Roadmap title", example = "12-Week Muscle Building Program")
        String title,

        @Schema(description = "Roadmap description", example = "A comprehensive program focused on building muscle mass")
        String description,

        @Schema(description = "List of stages in the roadmap")
        List<StageAIResponse> stages,

        @Schema(description = "Confidence score (0.0-1.0)", example = "0.85")
        @JsonProperty("confidenceScore")
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        BigDecimal confidenceScore,

        @Schema(description = "AI model used", example = "GEMINI_3_FLASH_PREVIEW")
        @JsonProperty("aiModel")
        AIModel aiModel,

        @Schema(description = "Generation timestamp", example = "2026-02-01T10:30:00Z")
        @JsonProperty("generatedAt")
        Instant generatedAt,

        @Schema(description = "Additional notes or warnings from AI")
        String notes
) {
    @Schema(description = "Stage information from AI")
    public record StageAIResponse(
            @Schema(description = "Stage name", example = "Foundation Phase", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("stageName")
            String stageName,

            @Schema(description = "Stage description", example = "Build foundational strength")
            String description,

            @Schema(description = "Stage order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("stageOrder")
            Integer stageOrder,

            @Schema(description = "Duration in weeks", example = "4", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("durationWeeks")
            Integer durationWeeks,

            @Schema(description = "List of schedules in this stage")
            List<ScheduleAIResponse> schedules,

            @Schema(description = "List of supplement recommendations for this stage")
            @JsonProperty("supplementRecommendations")
            List<SupplementRecommendationAIResponse> supplementRecommendations,

            @Schema(description = "Stage-specific notes or tips")
            String notes
    ) {}

    @Schema(description = "Schedule information from AI")
    public record ScheduleAIResponse(
            @Schema(description = "Schedule name", example = "Upper Body Workout", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("scheduleName")
            String scheduleName,

            @Schema(description = "Schedule description", example = "Focus on chest, back, and arms")
            String description,

            @Schema(description = "Day of week", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("dayOfWeek")
            String dayOfWeek,

            @Schema(description = "Scheduled date and time", example = "2026-01-24T07:00:00Z")
            Instant scheduledDate,

            @Schema(description = "Duration in minutes", example = "60", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("durationMinutes")
            Integer durationMinutes,

            @Schema(description = "List of exercises in this schedule")
            List<ExerciseAIResponse> exercises,

            @Schema(description = "Schedule-specific notes or tips")
            String notes
    ) {}

    @Schema(description = "Exercise information from AI")
    public record ExerciseAIResponse(
            @Schema(description = "Exercise name", example = "Bench Press", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("exerciseName")
            String exerciseName,

            @Schema(description = "Exercise Id", example = "123456")
            @JsonProperty("exerciseId")
            UUID exerciseId,

            @Schema(description = "Exercise image url", example = "url://BenchPress")
            @JsonProperty("imageUrl")
            String imageUrl,

            @Schema(description = "Exercise order", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            @JsonProperty("exerciseOrder")
            Integer exerciseOrder,

            @Schema(description = "Number of sets", example = "4")
            Integer sets,

            @Schema(description = "Number of reps", example = "12")
            Integer reps,

            @Schema(description = "Duration in seconds for time-based exercises", example = "45")
            @JsonProperty("durationSeconds")
            Integer durationSeconds,

            @Schema(description = "Rest time between sets in seconds", example = "60")
            @JsonProperty("restSeconds")
            Integer restSeconds,

            @Schema(description = "Target intensity (e.g., '75% 1RM', 'RPE 8')", example = "75% 1RM")
            String intensity,

            @Schema(description = "Notes for the exercise", example = "Keep your back pressed against the bench")
            String notes
    ) {}
}

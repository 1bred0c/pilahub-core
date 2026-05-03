package pilahub.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import pilahub.enums.AIModel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
            @Schema(description = "Stage name", example = "Foundation Phase")
            @JsonProperty("stageName")
            String stageName,

            @Schema(description = "Stage description", example = "Build foundational strength")
            String description,

            @Schema(description = "Stage order", example = "1")
            @JsonProperty("stageOrder")
            Integer stageOrder,

            @Schema(description = "Duration in weeks", example = "4")
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
            @Schema(description = "Schedule name", example = "Upper Body Workout")
            @JsonProperty("scheduleName")
            String scheduleName,

            @Schema(description = "Schedule description", example = "Focus on chest, back, and arms")
            String description,

            @Schema(description = "Day of week", example = "MONDAY")
            @JsonProperty("dayOfWeek")
            String dayOfWeek,

            @Schema(description = "Duration in minutes", example = "60")
            @JsonProperty("durationMinutes")
            Integer durationMinutes,

            @Schema(description = "List of exercises in this schedule")
            List<ExerciseAIResponse> exercises,

            @Schema(description = "Schedule-specific notes or tips")
            String notes
    ) {}

    @Schema(description = "Exercise information from AI")
    public record ExerciseAIResponse(
            @Schema(description = "Exercise name", example = "Bench Press")
            @JsonProperty("exerciseName")
            String exerciseName,

            @Schema(description = "Exercise order", example = "1")
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

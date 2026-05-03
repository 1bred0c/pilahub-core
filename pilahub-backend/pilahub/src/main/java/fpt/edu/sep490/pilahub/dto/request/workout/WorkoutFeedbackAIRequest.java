package fpt.edu.sep490.pilahub.dto.request.workout;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request to AI server for workout feedback generation")
public record WorkoutFeedbackAIRequest(
        @Schema(description = "Workout session ID", example = "550e8400-e29b-41d4-a716-446655440000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Workout session ID is required")
        String workoutSessionId,

        @Schema(description = "Trainee information", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Trainee info is required")
        TraineeInfo traineeInfo,

        @Schema(description = "Exercise information", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Exercise info is required")
        ExerciseInfo exerciseInfo,

        @Schema(description = "Session metrics", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Session metrics is required")
        SessionMetrics sessionMetrics,

        @Schema(description = "Mistake summary", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Mistake summary is required")
        MistakeSummary mistakeSummary,

        @Schema(description = "Video URL of the workout session")
        String videoUrl,

        @Schema(description = "Record URL (JSON data)")
        String recordUrl
) {
    @Schema(description = "Trainee information")
    public record TraineeInfo(
            @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Trainee ID is required")
            String traineeId,

            @Schema(description = "Trainee name", example = "Nguyễn Văn A", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Trainee name is required")
            String name,

            @Schema(description = "Trainee age", example = "28")
            Integer age,

            @Schema(description = "Fitness level", example = "INTERMEDIATE")
            String fitnessLevel,

            @Schema(description = "Experience in months", example = "6")
            Integer experienceMonths,

            @Schema(description = "Workout goals", example = "[\"Tăng sức mạnh cơ core\", \"Cải thiện tư thế\"]")
            List<String> goals,

            @Schema(description = "Current injuries", example = "[\"Lower back strain\"]")
            List<String> injuries
    ) {}

    @Schema(description = "Exercise information")
    public record ExerciseInfo(
            @Schema(description = "Exercise ID", example = "987e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Exercise ID is required")
            String exerciseId,

            @Schema(description = "Exercise name", example = "Pilates Hundred", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Exercise name is required")
            String name,

            @Schema(description = "Exercise description", example = "Classic Pilates breathing exercise")
            String description,

            @Schema(description = "Exercise type", example = "CORE_STRENGTH")
            String exerciseType,

            @Schema(description = "Difficulty level", example = "INTERMEDIATE")
            String difficultyLevel,

            @Schema(description = "Target body parts", example = "[\"Core\", \"Hip Flexors\"]")
            List<String> targetBodyParts,

            @Schema(description = "Exercise instructions", example = "Step-by-step guide...")
            String instructions,

            @Schema(description = "Common mistakes", example = "Lifting neck too high, not engaging core...")
            String commonMistakes,

            @Schema(description = "Expected duration in seconds", example = "180")
            Integer expectedDuration
    ) {}

    @Schema(description = "Session metrics")
    public record SessionMetrics(
            @Schema(description = "Total duration in seconds", example = "185.5", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Total duration is required")
            Double totalDuration,

            @Schema(description = "Completed reps", example = "10")
            Integer completedReps,

            @Schema(description = "Target reps", example = "12")
            Integer targetReps,

            @Schema(description = "Average heart rate", example = "125.3")
            Double averageHeartRate,

            @Schema(description = "Maximum heart rate", example = "145")
            Integer maxHeartRate,

            @Schema(description = "Calories burned", example = "85.2")
            Double caloriesBurned,

            @Schema(description = "Start time (ISO 8601)", example = "2026-03-12T07:00:00Z", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Start time is required")
            String startTime,

            @Schema(description = "End time (ISO 8601)", example = "2026-03-12T07:03:05.5Z", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "End time is required")
            String endTime,

            @Schema(description = "Had AI tracking", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Had AI tracking flag is required")
            Boolean hadAITracking,

            @Schema(description = "Had IOT tracking", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Had IOT tracking flag is required")
            Boolean hadIOTTracking
    ) {}
    @Schema(description = "Summary of mistakes during workout")
    public record MistakeSummary(
            @Schema(description = "Total number of mistakes", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Total mistakes is required")
            Integer totalMistakes,

            @Schema(description = "Detailed list of all individual mistakes", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Detailed mistakes list is required")
            List<MistakeDetail> detailedMistakes,

            @Schema(description = "Mistakes grouped by body part", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Mistakes by body part list is required")
            List<BodyPartMistake> mistakesByBodyPart,

            @Schema(description = "Average time between mistakes in seconds", example = "36.1")
            Double averageTimeBetweenMistakes,

            @Schema(description = "Total duration of all mistakes combined in seconds", example = "14.6")
            Double totalMistakeDuration,

            @Schema(description = "Percentage of workout time spent in mistake state", example = "7.9")
            Double mistakeTimePercentage
    ) {}

    @Schema(description = "Detailed information about a single mistake")
    public record MistakeDetail(
            @Schema(description = "Body part affected", example = "Lower Back", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Body part name is required")
            String bodyPartName,

            @Schema(description = "Time when mistake occurred (seconds from start)", example = "45.5", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Recorded at second is required")
            Double recordedAtSecond,

            @Schema(description = "Duration of the mistake in seconds", example = "3.2", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Duration is required")
            Double duration,

            @Schema(description = "Detailed description of the mistake", example = "Excessive lumbar extension detected", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Details is required")
            String details,

            @Schema(description = "Screenshot/frame URL showing the mistake", example = "https://storage.example.com/mistake_45s.jpg")
            String imageUrl
    ) {}

    @Schema(description = "Mistakes related to specific body part")
    public record BodyPartMistake(
            @Schema(description = "Body part name", example = "Lower Back", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Body part name is required")
            String bodyPartName,

            @Schema(description = "Number of mistakes", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Count is required")
            Integer count,

            @Schema(description = "Average severity (optional)", example = "2.7")
            Double averageSeverity,

            @Schema(description = "Total duration of mistakes for this body part in seconds", example = "9.6")
            Double totalDuration,

            @Schema(description = "Mistake details", example = "[\"Excessive arching at 45s\", \"Loss of neutral spine at 92s\"]", requiredMode = Schema.RequiredMode.REQUIRED)
            @NotNull(message = "Details list is required")
            List<String> details
    ) {}
}


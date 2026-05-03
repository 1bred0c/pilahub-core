package pilahub.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

/**
 * Request DTO cho Workout Feedback AI Analysis
 * Nhận input từ Backend Server chính theo specification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request for AI-powered workout feedback analysis")
public class WorkoutFeedbackAIRequest {

    @Schema(description = "Workout session ID", example = "550e8400-e29b-41d4-a716-446655440000", required = true)
    @NotBlank(message = "Workout session ID is required")
    @JsonProperty("workoutSessionId")
    private String workoutSessionId;

    @Schema(description = "Trainee information", required = true)
    @Valid
    @NotNull(message = "Trainee info is required")
    @JsonProperty("traineeInfo")
    private TraineeInfo traineeInfo;

    @Schema(description = "Exercise information", required = true)
    @Valid
    @NotNull(message = "Exercise info is required")
    @JsonProperty("exerciseInfo")
    private ExerciseInfo exerciseInfo;

    @Schema(description = "Session metrics", required = true)
    @Valid
    @NotNull(message = "Session metrics is required")
    @JsonProperty("sessionMetrics")
    private SessionMetrics sessionMetrics;

    @Schema(description = "Mistake summary - MOST IMPORTANT FOR AI", required = false)
    @Valid
    @JsonProperty("mistakeSummary")
    private MistakeSummary mistakeSummary;

    @Schema(description = "Video URL (optional)")
    @JsonProperty("videoUrl")
    private String videoUrl;

    @Schema(description = "Recording URL (optional)")
    @JsonProperty("recordUrl")
    private String recordUrl;

    // ============== NESTED CLASSES ==============

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TraineeInfo {
        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @NotBlank(message = "Trainee ID is required")
        @JsonProperty("traineeId")
        private String traineeId;

        @Schema(description = "Trainee name", example = "John Doe", required = true)
        @NotBlank(message = "Name is required")
        @JsonProperty("name")
        private String name;

        @Schema(description = "Fitness level", example = "INTERMEDIATE")
        @JsonProperty("fitnessLevel")
        private String fitnessLevel;

        @Schema(description = "Fitness goals", example = "[\"Build strength\", \"Improve flexibility\"]")
        @JsonProperty("goals")
        private List<String> goals;

        @Schema(description = "Known injuries or limitations", example = "[\"Previous lower back strain\"]")
        @JsonProperty("injuries")
        private List<String> injuries;

        @Schema(description = "Trainee age", example = "28")
        @Min(10)
        @Max(120)
        @JsonProperty("age")
        private Integer age;

        @Schema(description = "Experience in months", example = "6")
        @Min(0)
        @JsonProperty("experienceMonths")
        private Integer experienceMonths;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ExerciseInfo {
        @Schema(description = "Exercise ID", example = "987e4567-e89b-12d3-a456-426614174000", required = true)
        @NotBlank(message = "Exercise ID is required")
        @JsonProperty("exerciseId")
        private String exerciseId;

        @Schema(description = "Exercise name", example = "Pilates Hundred", required = true)
        @NotBlank(message = "Exercise name is required")
        @JsonProperty("name")
        private String name;

        @Schema(description = "Brief description")
        @JsonProperty("description")
        private String description;

        @Schema(description = "Exercise type", example = "STRENGTH")
        @JsonProperty("exerciseType")
        private String exerciseType;

        @Schema(description = "Difficulty level", example = "INTERMEDIATE")
        @JsonProperty("difficultyLevel")
        private String difficultyLevel;

        @Schema(description = "Target body parts", example = "[\"Core\", \"Hip Flexors\"]")
        @JsonProperty("targetBodyParts")
        private List<String> targetBodyParts;

        @Schema(description = "Step-by-step instructions")
        @JsonProperty("instructions")
        private String instructions;

        @Schema(description = "Common mistakes for this exercise")
        @JsonProperty("commonMistakes")
        private String commonMistakes;

        @Schema(description = "Expected duration in seconds", example = "180")
        @Min(0)
        @JsonProperty("expectedDuration")
        private Integer expectedDuration;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionMetrics {
        @Schema(description = "Total duration in seconds", example = "180.5", required = true)
        @NotNull(message = "Total duration is required")
        @Positive(message = "Duration must be positive")
        @JsonProperty("totalDuration")
        private Double totalDuration;

        @Schema(description = "Completed reps", example = "10")
        @Min(0)
        @JsonProperty("completedReps")
        private Integer completedReps;

        @Schema(description = "Target reps", example = "12")
        @Min(0)
        @JsonProperty("targetReps")
        private Integer targetReps;

        @Schema(description = "Average heart rate in bpm", example = "125.3")
        @Min(40)
        @Max(220)
        @JsonProperty("averageHeartRate")
        private Double averageHeartRate;

        @Schema(description = "Maximum heart rate in bpm", example = "145")
        @Min(40)
        @Max(220)
        @JsonProperty("maxHeartRate")
        private Integer maxHeartRate;

        @Schema(description = "Calories burned", example = "85.2")
        @Min(0)
        @JsonProperty("caloriesBurned")
        private Double caloriesBurned;

        @Schema(description = "Session start time", required = true)
        @NotNull(message = "Start time is required")
        @JsonProperty("startTime")
        private Instant startTime;

        @Schema(description = "Session end time", required = true)
        @NotNull(message = "End time is required")
        @JsonProperty("endTime")
        private Instant endTime;

        @Schema(description = "Whether AI tracking was enabled", example = "true", required = true)
        @NotNull(message = "hadAITracking is required")
        @JsonProperty("hadAITracking")
        private Boolean hadAITracking;

        @Schema(description = "Whether IoT tracking was enabled", example = "false", required = true)
        @NotNull(message = "hadIOTTracking is required")
        @JsonProperty("hadIOTTracking")
        private Boolean hadIOTTracking;
    }

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

        @Schema(description = "Detailed list of every mistake - CRITICAL FOR AI", required = false)
        @JsonProperty("detailedMistakes")
        private List<MistakeDetail> detailedMistakes;

        @Schema(description = "Mistakes grouped by body part", required = false)
        @JsonProperty("mistakesByBodyPart")
        private List<BodyPartMistake> mistakesByBodyPart;

        @Schema(description = "Average time between mistakes in seconds", example = "36.1")
        @Min(0)
        @JsonProperty("averageTimeBetweenMistakes")
        private Double averageTimeBetweenMistakes;

        @Schema(description = "Total mistake duration in seconds", example = "45.2")
        @Min(0)
        @JsonProperty("totalMistakeDuration")
        private Double totalMistakeDuration;

        @Schema(description = "Percentage of workout time with mistakes", example = "25.1")
        @Min(0)
        @Max(100)
        @JsonProperty("mistakeTimePercentage")
        private Double mistakeTimePercentage;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MistakeDetail {
        @Schema(description = "Affected body part", example = "Lower Back", required = true)
        @NotBlank(message = "Body part name is required")
        @JsonProperty("bodyPartName")
        private String bodyPartName;

        @Schema(description = "When mistake occurred (seconds from start)", example = "45.5", required = true)
        @NotNull(message = "Recorded time is required")
        @Min(0)
        @JsonProperty("recordedAtSecond")
        private Double recordedAtSecond;

        @Schema(description = "How long mistake lasted in seconds", example = "3.2", required = true)
        @NotNull(message = "Duration is required")
        @Min(0)
        @JsonProperty("duration")
        private Double duration;

        @Schema(description = "Human-readable description", example = "Excessive lumbar extension detected", required = true)
        @NotBlank(message = "Details are required")
        @JsonProperty("details")
        private String details;

        @Schema(description = "Screenshot URL showing the mistake")
        @JsonProperty("imageUrl")
        private String imageUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BodyPartMistake {
        @Schema(description = "Body part name", example = "Lower Back", required = true)
        @NotBlank(message = "Body part name is required")
        @JsonProperty("bodyPartName")
        private String bodyPartName;

        @Schema(description = "Number of mistakes", example = "3", required = true)
        @NotNull(message = "Count is required")
        @Min(0)
        @JsonProperty("count")
        private Integer count;

        @Schema(description = "Average severity", example = "2.7")
        @Min(0)
        @JsonProperty("averageSeverity")
        private Double averageSeverity;

        @Schema(description = "Total duration in seconds", example = "9.6")
        @Min(0)
        @JsonProperty("totalDuration")
        private Double totalDuration;

        @Schema(description = "Brief description of each mistake", required = false)
        @JsonProperty("details")
        private List<String> details;
    }
}


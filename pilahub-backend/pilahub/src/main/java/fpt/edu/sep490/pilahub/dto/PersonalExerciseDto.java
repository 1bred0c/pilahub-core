package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Personal exercise information")
public record PersonalExerciseDto(
                @Schema(description = "Unique personal exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID personalExerciseId,

                @Schema(description = "Personal schedule identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID personalScheduleId,

                @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID exerciseId,

                @Schema(description = "Exercise name", example = "Push-ups") String exerciseName,

                @Schema(description = "Exercise image URL", example = "https://example.com/images/pushup.jpg") String imageUrl,

                @Schema(description = "Exercise order number", example = "1") Integer exerciseOrder,

                @Schema(description = "Number of sets", example = "3") Integer sets,

                @Schema(description = "Number of reps", example = "15") Integer reps,

                @Schema(description = "Duration in seconds", example = "120") Integer durationSeconds,

                @Schema(description = "Rest time between sets in seconds", example = "60") Integer restSeconds,

                @Schema(description = "Personal notes", example = "Focus on form") String notes,

                @Schema(description = "Whether AI support is available for this exercise", example = "false") boolean haveAIsupported,

                @Schema(description = "Name in model AI", example = "plank") String nameInModelAI,

                @Schema(description = "Whether the exercise is completed", example = "false") boolean completed,

                @Schema(description = "Completion timestamp", example = "2026-01-24T08:00:00Z") Instant completedAt,

                @Schema(description = "Exercise creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt,

                @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z") Instant updatedAt) {
}

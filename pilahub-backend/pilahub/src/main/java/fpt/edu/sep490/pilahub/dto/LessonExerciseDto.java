package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Lesson exercise relationship information")
public record LessonExerciseDto(
        @Schema(description = "Unique lesson exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonExerciseId,

        @Schema(description = "Lesson ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonId,

        @Schema(description = "Lesson name", example = "Morning Stretching Routine")
        String lessonName,

        @Schema(description = "Exercise ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseId,

        @Schema(description = "Exercise name", example = "Push-ups")
        String exerciseName,

        @Schema(description = "Display order", example = "1")
        Integer displayOrder,

        @Schema(description = "Number of sets", example = "3")
        Integer sets,

        @Schema(description = "Number of reps", example = "12")
        Integer reps,

        @Schema(description = "Duration in seconds", example = "60")
        Integer durationSeconds,

        @Schema(description = "Rest time in seconds", example = "30")
        Integer restSeconds,

        @Schema(description = "Additional notes", example = "Focus on form")
        String notes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

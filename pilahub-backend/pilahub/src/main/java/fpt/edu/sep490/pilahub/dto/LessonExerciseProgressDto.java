package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Lesson exercise progress information for tracking individual exercise completion in a lesson")
public record LessonExerciseProgressDto(
        @Schema(description = "Unique lesson exercise progress identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonExerciseProgressId,

        @Schema(description = "Course lesson progress identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID courseLessonProgressId,

        @Schema(description = "Lesson exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonExerciseId,

        @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseId,

        @Schema(description = "Exercise name", example = "Push-ups")
        String exerciseName,

        @Schema(description = "Exercise start timestamp", example = "2026-01-24T08:00:00Z")
        Instant startedAt,

        @Schema(description = "Exercise completion timestamp", example = "2026-01-24T09:30:00Z")
        Instant completedAt,

        @Schema(description = "Whether the exercise is completed", example = "false")
        boolean completed,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}


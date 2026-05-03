package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Workout session information")
public record WorkoutSessionDto(
        @Schema(description = "Unique workout session identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID workoutSessionId,

        @Schema(description = "Trainee identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Personal exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalExerciseId,


        @Schema(description = "Lesson exercise progress identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID lessonExerciseProgressId,

        @Schema(description = "Exercise identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID exerciseId,

        @Schema(description = "Exercise name", example = "Push-ups")
        String exerciseName,

        @Schema(description = "Whether AI tracking is enabled", example = "false")
        boolean haveAITracking,

        @Schema(description = "Whether IoT device tracking is enabled", example = "false")
        boolean haveIOTDeviceTracking,

        @Schema(description = "Session start time", example = "2026-01-24T08:00:00Z")
        Instant startTime,

        @Schema(description = "Session end time", example = "2026-01-24T09:00:00Z")
        Instant endTime,

        @Schema(description = "Duration in seconds", example = "3600.0")
        Double durationSeconds,

        @Schema(description = "Recording URL from workout session (available for 7 days)", example = "https://storage.example.com/recordings/xyz123.mp4")
        String recordUrl,

        @Schema(description = "Whether the recording is still available (becomes false after 7 days)", example = "true")
        boolean recordAvailable,

        @Schema(description = "Whether the session is completed", example = "false")
        boolean completed,

        @Schema(description = "Session creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}



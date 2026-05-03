package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Heart rate log information")
public record HeartRateLogDto(
        @Schema(description = "Unique heart rate log identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID heartRateLogId,

        @Schema(description = "Workout session identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID workoutSessionId,

        @Schema(description = "Heart rate in bpm", example = "120")
        int heartRate,

        @Schema(description = "Recorded time in seconds from session start", example = "300")
        Integer recordedAt
) {
}


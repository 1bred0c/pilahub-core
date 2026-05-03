package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Real-time heart rate data from trainee to coach")
public record HeartRateDto(
        @Schema(description = "Live session identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID liveSessionId,

        @Schema(description = "Heart rate in BPM (beats per minute)", example = "120")
        Integer heartRate,

        @Schema(description = "Timestamp when heart rate was measured", example = "2026-03-08T14:05:00Z")
        Instant timestamp
) {
}


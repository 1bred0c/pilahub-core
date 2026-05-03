package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Coach time off information")
public record CoachTimeOffDto(
        @Schema(description = "Unique time off identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Coach information")
        CoachDto coach,

        @Schema(description = "Time off start time", example = "2026-03-05T10:00:00Z")
        Instant startTime,

        @Schema(description = "Time off end time", example = "2026-03-05T12:00:00Z")
        Instant endTime,

        @Schema(description = "Reason for time off", example = "Personal appointment")
        String reason,

        @Schema(description = "Time off creation timestamp", example = "2026-03-03T10:30:00Z")
        Instant createdAt
) {
}


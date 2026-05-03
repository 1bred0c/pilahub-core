package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.BusyScheduleType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Coach busy schedule item (booking or time off)")
public record CoachBusyScheduleDto(
        @Schema(description = "Unique identifier of the booking or time off", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,

        @Schema(description = "Type of busy schedule", example = "BOOKING")
        BusyScheduleType type,

        @Schema(description = "Start time", example = "2026-03-05T15:00:00Z")
        Instant startTime,

        @Schema(description = "End time", example = "2026-03-05T17:00:00Z")
        Instant endTime,

        @Schema(description = "Title/Description", example = "Training session with John Doe")
        String title,

        @Schema(description = "Additional details (trainee name for booking, reason for time off)")
        String details
) {
}


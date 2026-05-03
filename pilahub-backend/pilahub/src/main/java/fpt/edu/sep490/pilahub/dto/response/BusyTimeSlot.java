package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Simple time slot representing busy period")
public record BusyTimeSlot(
        @Schema(description = "Start time of busy period", example = "2026-03-15T10:00:00Z")
        Instant startTime,

        @Schema(description = "End time of busy period", example = "2026-03-15T12:00:00Z")
        Instant endTime
) {
}


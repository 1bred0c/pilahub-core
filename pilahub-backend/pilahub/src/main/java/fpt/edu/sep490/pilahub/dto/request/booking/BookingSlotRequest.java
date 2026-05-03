package fpt.edu.sep490.pilahub.dto.request.booking;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Single booking slot for batch booking")
public record BookingSlotRequest(
        @Schema(description = "Booking start time", example = "2026-03-05T15:00:00Z", required = true)
        Instant startTime,

        @Schema(description = "Booking end time", example = "2026-03-05T17:00:00Z", required = true)
        Instant endTime
) {
}


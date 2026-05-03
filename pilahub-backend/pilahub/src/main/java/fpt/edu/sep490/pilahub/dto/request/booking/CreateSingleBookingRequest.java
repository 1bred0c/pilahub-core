package fpt.edu.sep490.pilahub.dto.request.booking;

import fpt.edu.sep490.pilahub.enums.BookingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Request to create a single coach booking")
public record CreateSingleBookingRequest(
        @Schema(description = "Coach ID to book", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
        @NotNull(message = "Coach ID must not be null")
        UUID coachId,

        @Schema(description = "Booking start time", example = "2026-03-05T15:00:00Z", required = true)
        @NotNull(message = "Start time must not be null")
        Instant startTime,

        @Schema(description = "Booking end time (minimum 1 hour, maximum 4 hours)", example = "2026-03-05T17:00:00Z", required = true)
        @NotNull(message = "End time must not be null")
        Instant endTime,

        @Schema(description = "Booking type", example = "SINGLE", required = true)
        @NotNull(message = "Booking type must not be null")
        BookingType bookingType
) {
}


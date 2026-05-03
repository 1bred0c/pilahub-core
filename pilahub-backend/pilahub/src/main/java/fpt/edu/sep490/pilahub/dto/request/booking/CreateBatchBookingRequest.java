package fpt.edu.sep490.pilahub.dto.request.booking;

import fpt.edu.sep490.pilahub.enums.BookingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create multiple coach bookings at once")
public record CreateBatchBookingRequest(
                @Schema(description = "Coach ID to book", example = "123e4567-e89b-12d3-a456-426614174000", required = true) @NotNull(message = "Coach ID must not be null") UUID coachId,

                @Schema(description = "List of booking slots", required = true) @NotEmpty(message = "Booking slots must not be empty") @Valid List<BookingSlotRequest> bookingSlots,

                @Schema(description = "Booking type", example = "PERSONAL_TRAINING_PACKAGE", required = true) @NotNull(message = "Booking type must not be null") BookingType bookingType,

                @Schema(description = "Recurring group ID (optional, for package bookings)", example = "123e4567-e89b-12d3-a456-426614174000") UUID recurringGroupId) {
}

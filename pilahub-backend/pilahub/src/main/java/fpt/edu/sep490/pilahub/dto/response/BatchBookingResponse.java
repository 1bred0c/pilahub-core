package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.request.booking.BookingSlotRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response for batch booking creation with conflicts")
public record BatchBookingResponse(
        @Schema(description = "Successfully created bookings")
        List<CoachBookingDto> successfulBookings,

        @Schema(description = "Booking slots that have time conflicts")
        List<BookingSlotRequest> conflictingSlots,

        @Schema(description = "Total successful bookings count")
        int successCount,

        @Schema(description = "Total conflicting slots count")
        int conflictCount
) {
}


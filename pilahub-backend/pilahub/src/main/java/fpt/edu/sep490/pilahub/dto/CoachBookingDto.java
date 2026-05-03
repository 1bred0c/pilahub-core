package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.BookingType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Coach booking information")
public record CoachBookingDto(
                @Schema(description = "Unique booking identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID id,

                @Schema(description = "Coach information") CoachDto coach,

                @Schema(description = "Trainee information") TraineeDto trainee,

                @Schema(description = "Booking start time", example = "2026-03-05T15:00:00Z") Instant startTime,

                @Schema(description = "Booking end time", example = "2026-03-05T17:00:00Z") Instant endTime,

                @Schema(description = "Price per hour at time of booking", example = "500000.00") BigDecimal pricePerHour,

                @Schema(description = "Total amount for booking", example = "1000000.00") BigDecimal totalAmount,

                @Schema(description = "Booking status", example = "SCHEDULED") BookingStatus status,

                @Schema(description = "Booking type", example = "SINGLE") BookingType bookingType,

                @Schema(description = "Recurring group ID if part of a series", example = "123e4567-e89b-12d3-a456-426614174000") UUID recurringGroupId,

                @Schema(description = "Booking creation timestamp", example = "2026-03-03T10:30:00Z") Instant createdAt,

                @Schema(description = "Personal schedule for this session (only for PERSONAL_TRAINING_PACKAGE bookings, null for SINGLE)") PersonalScheduleDto personalSchedule) {
}

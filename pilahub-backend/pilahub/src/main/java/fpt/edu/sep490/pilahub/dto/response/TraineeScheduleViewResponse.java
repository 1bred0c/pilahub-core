package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.CoachBusyScheduleDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Combined schedule for trainee - includes coach's busy schedule and trainee's bookings")
public record TraineeScheduleViewResponse(
        @Schema(description = "Coach's busy schedule (time offs + active bookings)")
        List<CoachBusyScheduleDto> coachBusySchedule,

        @Schema(description = "Trainee's own bookings with this coach (excluding cancelled and refunded)")
        List<CoachBookingDto> traineeBookings
) {
}


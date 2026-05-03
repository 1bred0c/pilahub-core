package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Daily task feed for a trainee, including bookings, roadmap schedules, and course schedules")
public record DailyTaskResponse(
        @Schema(description = "Requested date") LocalDate date,

        @Schema(description = "Start of the requested day in UTC") Instant startOfDay,

        @Schema(description = "Start of the next day in UTC") Instant endOfDay,

        @Schema(description = "Coach bookings on the requested date") List<CoachBookingDto> bookings,

        @Schema(description = "Roadmap schedules on the requested date") List<PersonalScheduleDto> roadmapSchedules,

        @Schema(description = "Course lesson schedules on the requested date") List<CourseLessonProgressDto> courseSchedules) {
}
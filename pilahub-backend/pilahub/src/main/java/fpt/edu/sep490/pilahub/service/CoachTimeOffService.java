package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CoachBusyScheduleDto;
import fpt.edu.sep490.pilahub.dto.CoachTimeOffDto;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateCoachTimeOffRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CoachTimeOffService {

    // Coach creates a time off
    CoachTimeOffDto createTimeOff(UUID coachId, CreateCoachTimeOffRequest request);

    // Get time off by ID
    CoachTimeOffDto getTimeOffById(UUID timeOffId);

    // Get all time offs for a coach
    List<CoachTimeOffDto> getTimeOffsByCoach(UUID coachId);

    // Get time offs for a coach within a time range
    List<CoachTimeOffDto> getTimeOffsByCoachAndTimeRange(UUID coachId, Instant startTime, Instant endTime);

    // Delete a time off (Coach only, must be before it starts)
    void deleteTimeOff(UUID timeOffId, UUID coachId);

    // Admin can view all time offs
    List<CoachTimeOffDto> getAllTimeOffs();

    // Get total busy schedule for a coach (bookings + time offs)
    // Excludes cancelled and refunded bookings
    // Optional time range filter
    List<CoachBusyScheduleDto> getCoachBusySchedule(UUID coachId, Instant startTime, Instant endTime);
}


package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachBusyScheduleDto;
import fpt.edu.sep490.pilahub.dto.CoachTimeOffDto;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateCoachTimeOffRequest;
import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.BusyScheduleType;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CoachTimeOffMapper;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import fpt.edu.sep490.pilahub.pojo.CoachTimeOff;
import fpt.edu.sep490.pilahub.repository.CoachBookingRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.CoachTimeOffRepository;
import fpt.edu.sep490.pilahub.service.CoachTimeOffService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CoachTimeOffServiceImpl implements CoachTimeOffService {

    private final CoachTimeOffRepository coachTimeOffRepository;
    private final CoachRepository coachRepository;
    private final CoachBookingRepository coachBookingRepository;
    private final CoachTimeOffMapper coachTimeOffMapper;

    private static final int MAX_WEEKLY_TIME_OFF_HOURS = 8;
    private static final int MIN_ADVANCE_HOURS = 24;
    private static final int WORKING_START_HOUR = 6;
    private static final int WORKING_END_HOUR = 20;

    @Override
    public CoachTimeOffDto createTimeOff(UUID coachId, CreateCoachTimeOffRequest request) {
        // Validate coach
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        if (!coach.isActive()) {
            throw new IllegalStateException("Coach is not active");
        }

        // Validate time off request
        validateTimeOffRequest(request.startTime(), request.endTime());

        // Check if time off is at least 24 hours in advance
        Instant now = Instant.now();
        long hoursUntilStart = Duration.between(now, request.startTime()).toHours();

        if (hoursUntilStart < MIN_ADVANCE_HOURS) {
            throw new IllegalArgumentException(
                    String.format("Time off must be requested at least %d hours in advance", MIN_ADVANCE_HOURS)
            );
        }

        // Check for conflicts with existing time offs
        List<CoachTimeOff> existingTimeOffs = coachTimeOffRepository.findConflictingTimeOffs(
                coachId,
                request.startTime(),
                request.endTime()
        );

        if (!existingTimeOffs.isEmpty()) {
            throw new IllegalStateException("Time off conflicts with existing time off");
        }

        // Check for conflicts with existing bookings
        // Exclude: CANCELLED_BY_COACH, CANCELLED_BY_TRAINEE, REFUNDED, NO_SHOW_BY_COACH
        // Include all other statuses that represent actual busy time
        List<BookingStatus> activeBusyStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                BookingStatus.NO_SHOW_BY_TRAINEE  // Coach was present, trainee didn't show
        );

        List<CoachBooking> existingBookings = coachBookingRepository.findConflictingBookings(
                coachId,
                request.startTime(),
                request.endTime(),
                activeBusyStatuses
        );

        if (!existingBookings.isEmpty()) {
            throw new IllegalStateException("Time off conflicts with existing bookings. Please cancel bookings first.");
        }

        // Check weekly time off limit
        validateWeeklyTimeOffLimit(coachId, request.startTime(), request.endTime());

        // Create time off
        CoachTimeOff timeOff = CoachTimeOff.builder()
                .coach(coach)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .reason(request.reason())
                .build();

        CoachTimeOff savedTimeOff = coachTimeOffRepository.save(timeOff);
        return coachTimeOffMapper.toDto(savedTimeOff);
    }

    @Override
    public CoachTimeOffDto getTimeOffById(UUID timeOffId) {
        CoachTimeOff timeOff = coachTimeOffRepository.findById(timeOffId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachTimeOff", "id", timeOffId));
        return coachTimeOffMapper.toDto(timeOff);
    }

    @Override
    public List<CoachTimeOffDto> getTimeOffsByCoach(UUID coachId) {
        List<CoachTimeOff> timeOffs = coachTimeOffRepository.findByCoach_CoachId(coachId);
        return timeOffs.stream()
                .map(coachTimeOffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachTimeOffDto> getTimeOffsByCoachAndTimeRange(UUID coachId, Instant startTime, Instant endTime) {
        List<CoachTimeOff> timeOffs = coachTimeOffRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        return timeOffs.stream()
                .map(coachTimeOffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteTimeOff(UUID timeOffId, UUID coachId) {
        CoachTimeOff timeOff = coachTimeOffRepository.findById(timeOffId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachTimeOff", "id", timeOffId));

        // Verify ownership
        if (!timeOff.getCoach().getCoachId().equals(coachId)) {
            throw new IllegalStateException("You can only delete your own time offs");
        }

        // Can only delete if it hasn't started yet
        Instant now = Instant.now();
        if (timeOff.getStartTime().isBefore(now)) {
            throw new IllegalStateException("Cannot delete time off that has already started");
        }

        // Must be at least 24 hours before start time
        long hoursUntilStart = Duration.between(now, timeOff.getStartTime()).toHours();
        if (hoursUntilStart < MIN_ADVANCE_HOURS) {
            throw new IllegalStateException(
                    String.format("Time off can only be deleted at least %d hours in advance", MIN_ADVANCE_HOURS)
            );
        }

        coachTimeOffRepository.delete(timeOff);
    }

    @Override
    public List<CoachTimeOffDto> getAllTimeOffs() {
        List<CoachTimeOff> timeOffs = coachTimeOffRepository.findAll();
        return timeOffs.stream()
                .map(coachTimeOffMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachBusyScheduleDto> getCoachBusySchedule(UUID coachId, Instant startTime, Instant endTime) {
        // Validate coach exists
        if (!coachRepository.existsById(coachId)) {
            throw new ResourceNotFoundException("Coach", "id", coachId);
        }

        List<CoachBusyScheduleDto> busySchedules = new ArrayList<>();

        // Get time offs (all of them are valid busy periods)
        List<CoachTimeOff> timeOffs;
        if (startTime != null && endTime != null) {
            timeOffs = coachTimeOffRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            timeOffs = coachTimeOffRepository.findByCoach_CoachId(coachId);
        }

        // Convert time offs to busy schedule items
        for (CoachTimeOff timeOff : timeOffs) {
            busySchedules.add(new CoachBusyScheduleDto(
                    timeOff.getId(),
                    BusyScheduleType.TIME_OFF,
                    timeOff.getStartTime(),
                    timeOff.getEndTime(),
                    "Time Off",
                    timeOff.getReason() != null ? timeOff.getReason() : "No reason provided"
            ));
        }

        // Get active bookings (exclude cancelled and refunded)
        // Active statuses: SCHEDULED, READY, IN_PROGRESS, COMPLETED, NO_SHOW_BY_TRAINEE
        // Exclude: CANCELLED_BY_COACH, CANCELLED_BY_TRAINEE, REFUNDED, NO_SHOW_BY_COACH
        List<BookingStatus> activeBusyStatuses = Arrays.asList(
                BookingStatus.SCHEDULED,
                BookingStatus.READY,
                BookingStatus.IN_PROGRESS,
                BookingStatus.COMPLETED,
                BookingStatus.NO_SHOW_BY_TRAINEE  // Coach was ready but trainee didn't show up
        );

        List<CoachBooking> bookings;
        if (startTime != null && endTime != null) {
            bookings = coachBookingRepository.findByCoachIdAndTimeRange(coachId, startTime, endTime);
        } else {
            bookings = coachBookingRepository.findByCoach_CoachId(coachId);
        }

        // Filter bookings by active status
        bookings = bookings.stream()
                .filter(booking -> activeBusyStatuses.contains(booking.getStatus()))
                .collect(Collectors.toList());

        // Convert bookings to busy schedule items
        for (CoachBooking booking : bookings) {
            String traineeName = booking.getTrainee().getFullName();
            String title = "Training Session";
            String details = String.format("With %s - Status: %s", traineeName, booking.getStatus());

            busySchedules.add(new CoachBusyScheduleDto(
                    booking.getId(),
                    BusyScheduleType.BOOKING,
                    booking.getStartTime(),
                    booking.getEndTime(),
                    title,
                    details
            ));
        }

        // Sort by start time
        busySchedules.sort(Comparator.comparing(CoachBusyScheduleDto::startTime));

        return busySchedules;
    }

    // Helper methods

    private void validateTimeOffRequest(Instant startTime, Instant endTime) {
        Instant now = Instant.now();

        // Start time must be in the future
        if (startTime.isBefore(now)) {
            throw new IllegalArgumentException("Start time must be in the future");
        }

        // Start time must be before end time
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // Calculate duration
        long hours = Duration.between(startTime, endTime).toHours();

        if (hours < 1) {
            throw new IllegalArgumentException("Minimum time off duration is 1 hour");
        }

        // Check if time off is within working hours (6:00 - 20:00)
        ZonedDateTime startZoned = startTime.atZone(ZoneId.systemDefault());
        ZonedDateTime endZoned = endTime.atZone(ZoneId.systemDefault());

        if (startZoned.getHour() < WORKING_START_HOUR || endZoned.getHour() > WORKING_END_HOUR) {
            throw new IllegalArgumentException(String.format(
                    "Time off must be within working hours (%d:00 - %d:00)",
                    WORKING_START_HOUR,
                    WORKING_END_HOUR
            ));
        }

        // If end time hour is exactly 20, check minutes
        if (endZoned.getHour() == WORKING_END_HOUR && endZoned.getMinute() > 0) {
            throw new IllegalArgumentException(String.format(
                    "Time off must end by %d:00",
                    WORKING_END_HOUR
            ));
        }
    }

    private void validateWeeklyTimeOffLimit(UUID coachId, Instant newStartTime, Instant newEndTime) {
        // Get the week boundaries for the requested time off
        ZonedDateTime startZoned = newStartTime.atZone(ZoneId.systemDefault());
        ZonedDateTime weekStart = startZoned.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                .withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime weekEnd = weekStart.plusWeeks(1);

        // Get all time offs for this coach in this week
        List<CoachTimeOff> weekTimeOffs = coachTimeOffRepository.findByCoachIdAndWeek(
                coachId,
                weekStart.toInstant(),
                weekEnd.toInstant()
        );

        // Calculate total hours of time off for the week
        long totalHours = weekTimeOffs.stream()
                .mapToLong(timeOff -> Duration.between(timeOff.getStartTime(), timeOff.getEndTime()).toHours())
                .sum();

        // Add the new time off duration
        long newHours = Duration.between(newStartTime, newEndTime).toHours();
        long totalWithNew = totalHours + newHours;

        if (totalWithNew > MAX_WEEKLY_TIME_OFF_HOURS) {
            throw new IllegalStateException(String.format(
                    "Weekly time off limit exceeded. Current: %d hours, Requested: %d hours, Max: %d hours",
                    totalHours,
                    newHours,
                    MAX_WEEKLY_TIME_OFF_HOURS
            ));
        }
    }
}



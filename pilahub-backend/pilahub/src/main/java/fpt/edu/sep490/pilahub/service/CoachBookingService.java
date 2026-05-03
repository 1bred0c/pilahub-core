package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateBatchBookingRequest;
import fpt.edu.sep490.pilahub.dto.request.booking.CreateSingleBookingRequest;
import fpt.edu.sep490.pilahub.dto.response.BatchBookingResponse;
import fpt.edu.sep490.pilahub.dto.response.BusyTimeSlot;
import fpt.edu.sep490.pilahub.enums.BookingStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface CoachBookingService {

    // Trainee creates a single booking (with payment)
    CoachBookingDto createSingleBooking(UUID traineeId, CreateSingleBookingRequest request);

    // Trainee creates multiple bookings at once (with payment)
    BatchBookingResponse createBatchBooking(UUID traineeId, CreateBatchBookingRequest request);

    // Get booking by ID
    CoachBookingDto getBookingById(UUID bookingId);

    // Get all bookings for a coach
    List<CoachBookingDto> getBookingsByCoach(UUID coachId);

    // Get all bookings for a trainee
    List<CoachBookingDto> getBookingsByTrainee(UUID traineeId);

    // Get bookings for a coach by status
    List<CoachBookingDto> getBookingsByCoachAndStatus(UUID coachId, BookingStatus status);

    // Get bookings for a trainee by status
    List<CoachBookingDto> getBookingsByTraineeAndStatus(UUID traineeId, BookingStatus status);

    // Get bookings for a coach within a time range
    List<CoachBookingDto> getBookingsByCoachAndTimeRange(UUID coachId, Instant startTime, Instant endTime);

    // Get bookings for a trainee within a time range
    List<CoachBookingDto> getBookingsByTraineeAndTimeRange(UUID traineeId, Instant startTime, Instant endTime);

    // Get bookings by recurring group ID
    List<CoachBookingDto> getBookingsByRecurringGroup(UUID recurringGroupId);

    // Cancel a booking (Trainee or Coach)
    void cancelBooking(UUID bookingId, UUID userId);

    // Coach joins the session (READY -> IN_PROGRESS)
    void coachJoinSession(UUID bookingId, UUID coachId);

    // Trainee joins the session (READY -> IN_PROGRESS)
    void traineeJoinSession(UUID bookingId, UUID traineeId);

    // Complete a booking (IN_PROGRESS -> COMPLETED)
    void completeBooking(UUID bookingId, UUID userId);

    // Admin can view all bookings
    List<CoachBookingDto> getAllBookings();

    // Update booking status (Admin only for some status transitions)
    void updateBookingStatus(UUID bookingId, BookingStatus newStatus);

    // Get combined busy time slots for trainee view (coach's busy schedule + trainee's bookings)
    // Returns a simple list of time slots (startTime, endTime) representing all busy periods
    List<BusyTimeSlot> getTraineeScheduleView(UUID traineeId, UUID coachId, Instant startTime, Instant endTime);
}



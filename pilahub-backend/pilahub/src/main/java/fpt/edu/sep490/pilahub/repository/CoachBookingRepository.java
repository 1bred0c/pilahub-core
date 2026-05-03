package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoachBookingRepository extends JpaRepository<CoachBooking, UUID> {

        List<CoachBooking> findByCoach_CoachId(UUID coachId);

        List<CoachBooking> findByTrainee_TraineeId(UUID traineeId);

        List<CoachBooking> findByCoach_CoachIdAndStatus(UUID coachId, BookingStatus status);

        List<CoachBooking> findByTrainee_TraineeIdAndStatus(UUID traineeId, BookingStatus status);

        List<CoachBooking> findByRecurringGroupId(UUID recurringGroupId);

        // Check for time conflicts for a coach
        @Query("SELECT cb FROM CoachBooking cb WHERE cb.coach.coachId = :coachId " +
                        "AND cb.status IN :validStatuses " +
                        "AND ((cb.startTime < :endTime AND cb.endTime > :startTime))")
        List<CoachBooking> findConflictingBookings(
                        @Param("coachId") UUID coachId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime,
                        @Param("validStatuses") List<BookingStatus> validStatuses);

        // Get bookings for a coach within a time range
        @Query("SELECT cb FROM CoachBooking cb WHERE cb.coach.coachId = :coachId " +
                        "AND cb.startTime >= :startTime AND cb.endTime <= :endTime " +
                        "ORDER BY cb.startTime ASC")
        List<CoachBooking> findByCoachIdAndTimeRange(
                        @Param("coachId") UUID coachId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);

        // Get bookings for a trainee within a time range
        @Query("SELECT cb FROM CoachBooking cb WHERE cb.trainee.traineeId = :traineeId " +
                        "AND cb.startTime >= :startTime AND cb.endTime <= :endTime " +
                        "ORDER BY cb.startTime ASC")
        List<CoachBooking> findByTraineeIdAndTimeRange(
                        @Param("traineeId") UUID traineeId,
                        @Param("startTime") Instant startTime,
                        @Param("endTime") Instant endTime);

        List<CoachBooking> findByTrainee_TraineeIdAndStartTimeLessThanAndEndTimeGreaterThanOrderByStartTimeAsc(
                        UUID traineeId,
                        Instant endTime,
                        Instant startTime);

        // Find bookings that should transition to READY status (start time has arrived)
        @Query("SELECT cb FROM CoachBooking cb WHERE cb.status = 'SCHEDULED' " +
                        "AND cb.startTime <= :currentTime")
        List<CoachBooking> findScheduledBookingsReadyToStart(@Param("currentTime") Instant currentTime);
    // Find bookings that should transition to READY status (5 minutes before start)

    // Find READY bookings that have been waiting for 15 minutes without both parties joining
    @Query("SELECT cb FROM CoachBooking cb WHERE cb.status = 'READY' " +
            "AND cb.startTime <= :fifteenMinutesAgo")
    List<CoachBooking> findReadyBookingsOverdue(@Param("fifteenMinutesAgo") Instant fifteenMinutesAgo);

    // Find IN_PROGRESS bookings that reached their end time
    @Query("SELECT cb FROM CoachBooking cb WHERE cb.status = 'IN_PROGRESS' " +
            "AND cb.endTime <= :currentTime")
    List<CoachBooking> findInProgressBookingsEndingBefore(@Param("currentTime") Instant currentTime);

}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveSessionRepository extends JpaRepository<LiveSession, UUID> {

    Optional<LiveSession> findByLiveSessionId(UUID liveSessionId);

    List<LiveSession> findByStatus(LiveSessionStatus status);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.coachBooking.coach.coachId = :coachId ORDER BY ls.createdAt DESC")
    List<LiveSession> findByCoachId(@Param("coachId") UUID coachId);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.coachBooking.trainee.traineeId = :traineeId ORDER BY ls.createdAt DESC")
    List<LiveSession> findByTraineeId(@Param("traineeId") UUID traineeId);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = :status AND ls.coachBooking.startTime <= :time")
    List<LiveSession> findByStatusAndStartTimeBefore(@Param("status") LiveSessionStatus status, @Param("time") Instant time);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'ACTIVE' AND ls.coachBooking.endTime <= :time")
    List<LiveSession> findActiveSessionsEndingBefore(@Param("time") Instant time);

    @Query("SELECT ls FROM LiveSession ls WHERE ls.status = 'ACTIVE' AND " +
           "(ls.coachJoinedAt IS NULL OR ls.traineeJoinedAt IS NULL) AND " +
           "ls.coachBooking.startTime <= :checkTime")
    List<LiveSession> findActiveSessionsWithMissingParticipants(@Param("checkTime") Instant checkTime);

    boolean existsByLiveSessionId(UUID liveSessionId);
}


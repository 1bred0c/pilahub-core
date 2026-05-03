package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.CoachTimeOff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface CoachTimeOffRepository extends JpaRepository<CoachTimeOff, UUID> {

    List<CoachTimeOff> findByCoach_CoachId(UUID coachId);

    // Check for time conflicts for a coach's time off
    @Query("SELECT cto FROM CoachTimeOff cto WHERE cto.coach.coachId = :coachId " +
            "AND ((cto.startTime < :endTime AND cto.endTime > :startTime))")
    List<CoachTimeOff> findConflictingTimeOffs(
            @Param("coachId") UUID coachId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );

    // Get total hours of time off for a coach in a given week
    @Query("SELECT cto FROM CoachTimeOff cto WHERE cto.coach.coachId = :coachId " +
            "AND cto.startTime >= :weekStart AND cto.endTime <= :weekEnd")
    List<CoachTimeOff> findByCoachIdAndWeek(
            @Param("coachId") UUID coachId,
            @Param("weekStart") Instant weekStart,
            @Param("weekEnd") Instant weekEnd
    );

    // Get time offs for a coach within a time range
    @Query("SELECT cto FROM CoachTimeOff cto WHERE cto.coach.coachId = :coachId " +
            "AND cto.startTime >= :startTime AND cto.endTime <= :endTime " +
            "ORDER BY cto.startTime ASC")
    List<CoachTimeOff> findByCoachIdAndTimeRange(
            @Param("coachId") UUID coachId,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime
    );
}


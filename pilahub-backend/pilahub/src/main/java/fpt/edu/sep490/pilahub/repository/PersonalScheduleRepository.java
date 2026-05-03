package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PersonalSchedule;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalScheduleRepository extends JpaRepository<PersonalSchedule, UUID> {

        List<PersonalSchedule> findByPersonalStage(PersonalStage personalStage);

        List<PersonalSchedule> findByPersonalStageOrderByScheduledDateAsc(PersonalStage personalStage);

        List<PersonalSchedule> findByCompletedTrue();

        List<PersonalSchedule> findByCompletedFalse();

        boolean existsByPersonalStageAndCompletedFalse(PersonalStage personalStage);

        Optional<PersonalSchedule> findByPersonalScheduleId(UUID personalScheduleId);

        List<PersonalSchedule> findByScheduledDateBetween(Instant startDate, Instant endDate);

        List<PersonalSchedule> findByPersonalStage_Roadmap_Trainee_TraineeIdAndScheduledDateGreaterThanEqualAndScheduledDateLessThanOrderByScheduledDateAsc(
                        UUID traineeId,
                        Instant startDate,
                        Instant endDate);

        boolean existsByPersonalScheduleId(UUID personalScheduleId);

        @Query("SELECT COUNT(sc) FROM PersonalSchedule sc " +
                        "JOIN sc.personalStage ps " +
                        "JOIN ps.roadmap r " +
                        "WHERE r.roadmapId = :roadmapId")
        int countTotalSchedulesInRoadmap(@Param("roadmapId") UUID roadmapId);

        @Query("SELECT COUNT(sc) FROM PersonalSchedule sc " +
                        "JOIN sc.personalStage ps " +
                        "JOIN ps.roadmap r " +
                        "WHERE r.roadmapId = :roadmapId AND sc.completed = true")
        int countCompletedSchedulesInRoadmap(@Param("roadmapId") UUID roadmapId);

        @Query("SELECT sc FROM PersonalSchedule sc " +
                        "JOIN sc.personalStage ps " +
                        "JOIN ps.roadmap r " +
                        "WHERE r.roadmapId = :roadmapId " +
                        "AND sc.scheduledDate >= :startOfDay AND sc.scheduledDate < :endOfDay")
        Optional<PersonalSchedule> findByRoadmapIdAndDate(
                        @Param("roadmapId") UUID roadmapId,
                        @Param("startOfDay") Instant startOfDay,
                        @Param("endOfDay") Instant endOfDay);
}

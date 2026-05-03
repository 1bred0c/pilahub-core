package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PersonalExercise;
import fpt.edu.sep490.pilahub.pojo.PersonalSchedule;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalExerciseRepository extends JpaRepository<PersonalExercise, UUID> {

    List<PersonalExercise> findByPersonalSchedule(PersonalSchedule personalSchedule);

    List<PersonalExercise> findByPersonalScheduleOrderByExerciseOrderAsc(PersonalSchedule personalSchedule);

    List<PersonalExercise> findByExercise(Exercise exercise);

    List<PersonalExercise> findByCompletedTrue();

    List<PersonalExercise> findByCompletedFalse();

    Optional<PersonalExercise> findByPersonalExerciseId(UUID personalExerciseId);

    @Query("SELECT DISTINCT eq.name FROM PersonalExercise pe " +
            "JOIN pe.personalSchedule ps " +
            "JOIN ps.personalStage pst " +
            "JOIN pst.roadmap r " +
            "JOIN pe.exercise e " +
            "JOIN ExerciseEquipment ee ON ee.exercise = e " +
            "JOIN ee.equipment eq " +
            "WHERE r.roadmapId = :roadmapId")
    List<String> findEquipmentNamesByRoadmapId(@Param("roadmapId") UUID roadmapId);

    @Query("SELECT DISTINCT eq.equipmentId FROM PersonalExercise pe " +
            "JOIN pe.personalSchedule ps " +
            "JOIN ps.personalStage pst " +
            "JOIN pst.roadmap r " +
            "JOIN pe.exercise e " +
            "JOIN ExerciseEquipment ee ON ee.exercise = e " +
            "JOIN ee.equipment eq " +
            "WHERE r.roadmapId = :roadmapId")
    List<UUID> findEquipmentIdsByRoadmapId(@Param("roadmapId") UUID roadmapId);

    @Query("SELECT COUNT(pe) FROM PersonalExercise pe " +
            "JOIN pe.personalSchedule ps " +
            "JOIN ps.personalStage pst " +
            "JOIN pst.roadmap r " +
            "WHERE r.roadmapId = :roadmapId")
    int countTotalExercisesInRoadmap(@Param("roadmapId") UUID roadmapId);

    @Query("SELECT COUNT(pe) FROM PersonalExercise pe " +
            "JOIN pe.personalSchedule ps " +
            "JOIN ps.personalStage pst " +
            "JOIN pst.roadmap r " +
            "WHERE r.roadmapId = :roadmapId AND pe.completed = true")
    int countCompletedExercisesInRoadmap(@Param("roadmapId") UUID roadmapId);
}

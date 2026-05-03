package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface WorkoutSessionRepository extends JpaRepository<WorkoutSession, UUID> {

    List<WorkoutSession> findByTrainee(Trainee trainee);

    List<WorkoutSession> findByTrainee_TraineeIdOrderByCreatedAtDesc(UUID traineeId);

    List<WorkoutSession> findByTrainee_TraineeIdAndCompletedTrue(UUID traineeId);

    List<WorkoutSession> findByTrainee_TraineeIdAndCompletedFalse(UUID traineeId);

    List<WorkoutSession> findByExercise_ExerciseId(UUID exerciseId);

    List<WorkoutSession> findByTrainee_TraineeIdAndStartTimeBetween(UUID traineeId, Instant from, Instant to);

    List<WorkoutSession> findByPersonalExercise_PersonalExerciseId(UUID personalExerciseId);

    List<WorkoutSession> findByLessonExerciseProgress_LessonExerciseProgressId(UUID lessonExerciseProgressId);

    List<WorkoutSession> findByCompletedTrueAndRecordAvailableTrueAndEndTimeBefore(Instant endTime);

    boolean existsByTrainee_TraineeIdAndExercise_ExerciseId(UUID traineeId, UUID exerciseId);

    @Query("SELECT DISTINCT ws.exercise.exerciseId FROM WorkoutSession ws WHERE ws.trainee.traineeId = :traineeId")
    Set<UUID> findDistinctExerciseIdsByTraineeId(@Param("traineeId") UUID traineeId);
}

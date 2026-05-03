package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.WorkoutFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutFeedbackRepository extends JpaRepository<WorkoutFeedback, UUID> {

    Optional<WorkoutFeedback> findByWorkoutSession_WorkoutSessionId(UUID workoutSessionId);

    List<WorkoutFeedback> findByWorkoutSession_Trainee_TraineeIdOrderByGeneratedAtDesc(UUID traineeId);

    boolean existsByWorkoutSession_WorkoutSessionId(UUID workoutSessionId);

    void deleteByWorkoutSession_WorkoutSessionId(UUID workoutSessionId);
}


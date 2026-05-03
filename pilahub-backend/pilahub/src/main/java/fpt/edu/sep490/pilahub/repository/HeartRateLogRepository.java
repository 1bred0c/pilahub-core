package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.HeartRateLog;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HeartRateLogRepository extends JpaRepository<HeartRateLog, UUID> {

    List<HeartRateLog> findByWorkoutSession(WorkoutSession workoutSession);

    List<HeartRateLog> findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(UUID workoutSessionId);

    void deleteByWorkoutSession_WorkoutSessionId(UUID workoutSessionId);
}


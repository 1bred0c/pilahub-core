package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.MistakeLog;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MistakeLogRepository extends JpaRepository<MistakeLog, UUID> {

    List<MistakeLog> findByWorkoutSession(WorkoutSession workoutSession);

    List<MistakeLog> findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(UUID workoutSessionId);

    List<MistakeLog> findByBodyPart_BodyPartId(UUID bodyPartId);

    void deleteByWorkoutSession_WorkoutSessionId(UUID workoutSessionId);
}


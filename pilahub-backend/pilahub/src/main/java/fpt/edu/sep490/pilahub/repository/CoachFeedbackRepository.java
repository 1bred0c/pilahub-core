package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.CoachFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoachFeedbackRepository extends JpaRepository<CoachFeedback, UUID> {

    List<CoachFeedback> findByCoach_CoachId(UUID coachId);

    List<CoachFeedback> findByTrainee_TraineeId(UUID traineeId);

    boolean existsByCoach_CoachIdAndTrainee_TraineeId(UUID coachId, UUID traineeId);

    @Query("SELECT AVG(cf.rating) FROM CoachFeedback cf WHERE cf.coach.coachId = :coachId")
    Double calculateAverageRatingByCoachId(@Param("coachId") UUID coachId);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.CoachRoadmapRequestStatus;
import fpt.edu.sep490.pilahub.pojo.CoachRoadmapRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoachRoadmapRequestRepository extends JpaRepository<CoachRoadmapRequest, UUID> {

    List<CoachRoadmapRequest> findByTrainee_TraineeIdOrderByCreatedAtDesc(UUID traineeId);

    List<CoachRoadmapRequest> findByCoach_CoachIdOrderByCreatedAtDesc(UUID coachId);

    List<CoachRoadmapRequest> findByCoach_CoachIdAndStatusOrderByCreatedAtDesc(
            UUID coachId, CoachRoadmapRequestStatus status);

    boolean existsByTrainee_TraineeIdAndCoach_CoachIdAndStatusIn(
            UUID traineeId, UUID coachId, List<CoachRoadmapRequestStatus> statuses);
}

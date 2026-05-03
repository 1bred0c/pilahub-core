package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.SessionAssessment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionAssessmentRepository extends JpaRepository<SessionAssessment, UUID> {

    boolean existsByLiveSessionId(UUID liveSessionId);

    @EntityGraph(attributePaths = {"results", "results.criterion"})
    Optional<SessionAssessment> findWithResultsByLiveSessionId(UUID liveSessionId);

    @EntityGraph(attributePaths = {"results", "results.criterion"})
    List<SessionAssessment> findByTraineeIdOrderBySubmittedAtAsc(UUID traineeId);
}


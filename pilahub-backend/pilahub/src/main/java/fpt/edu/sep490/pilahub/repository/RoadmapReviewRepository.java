package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.RoadmapReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapReviewRepository extends JpaRepository<RoadmapReview, UUID> {

    Optional<RoadmapReview> findByRoadmap_RoadmapId(UUID roadmapId);

    boolean existsByRoadmap_RoadmapId(UUID roadmapId);
}


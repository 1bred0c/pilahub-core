package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalStageRepository extends JpaRepository<PersonalStage, UUID> {

    List<PersonalStage> findByRoadmap_RoadmapId(UUID roadmapId);

    List<PersonalStage> findByRoadmapOrderByStageOrderAsc(Roadmap roadmap);

    List<PersonalStage> findByCompletedTrue();

    List<PersonalStage> findByCompletedFalse();

    Optional<PersonalStage> findByPersonalStageId(UUID personalStageId);

    boolean existsByPersonalStageId(UUID personalStageId);
}

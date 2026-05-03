package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.RoadmapStatus;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.Coach;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, UUID>, JpaSpecificationExecutor<Roadmap> {

    List<Roadmap> findByTitleContainingIgnoreCase(String title);

    // Get the newest roadmap for a trainee (ordered by createdAt desc, limit 1)
    Optional<Roadmap> findFirstByTraineeAndStatusOrderByCreatedAtDesc(
            Trainee trainee,
            RoadmapStatus status
    );
}

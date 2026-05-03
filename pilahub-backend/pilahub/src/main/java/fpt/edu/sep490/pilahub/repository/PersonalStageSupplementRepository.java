package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PersonalStageSupplement;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PersonalStageSupplementRepository extends JpaRepository<PersonalStageSupplement, UUID> {

    List<PersonalStageSupplement> findByPersonalStage(PersonalStage personalStage);

    List<PersonalStageSupplement> findByPersonalStage_PersonalStageId(UUID personalStageId);

    List<PersonalStageSupplement> findBySupplement_SupplementId(UUID supplementId);

    List<PersonalStageSupplement> findByPersonalStage_Roadmap_RoadmapId(UUID roadmapId);

    Optional<PersonalStageSupplement> findByPersonalStage_PersonalStageIdAndSupplement_SupplementId(
            UUID personalStageId, UUID supplementId);

    boolean existsByPersonalStage_PersonalStageIdAndSupplement_SupplementId(UUID personalStageId, UUID supplementId);

    void deleteByPersonalStage_PersonalStageId(UUID personalStageId);

    @Query("SELECT DISTINCT s.name FROM PersonalStageSupplement pss " +
            "JOIN pss.personalStage ps " +
            "JOIN ps.roadmap r " +
            "JOIN pss.supplement s " +
            "WHERE r.roadmapId = :roadmapId")
    List<String> findSupplementNamesByRoadmapId(@Param("roadmapId") UUID roadmapId);

    @Query("SELECT DISTINCT s.supplementId FROM PersonalStageSupplement pss " +
            "JOIN pss.personalStage ps " +
            "JOIN ps.roadmap r " +
            "JOIN pss.supplement s " +
            "WHERE r.roadmapId = :roadmapId")
    List<UUID> findSupplementIdsByRoadmapId(@Param("roadmapId") UUID roadmapId);
}

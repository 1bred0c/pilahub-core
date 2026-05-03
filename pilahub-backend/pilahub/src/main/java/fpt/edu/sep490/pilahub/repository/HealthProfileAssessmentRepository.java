package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.HealthProfileAssessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthProfileAssessmentRepository extends JpaRepository<HealthProfileAssessment, UUID> {

    /**
     * Find assessment by health profile ID
     */
    @Query("SELECT hpa FROM HealthProfileAssessment hpa WHERE hpa.healthProfile.healthProfileId = :healthProfileId")
    Optional<HealthProfileAssessment> findByHealthProfileId(@Param("healthProfileId") UUID healthProfileId);

    /**
     * Delete assessment by health profile ID
     */
    void deleteByHealthProfile_HealthProfileId(UUID healthProfileId);
}

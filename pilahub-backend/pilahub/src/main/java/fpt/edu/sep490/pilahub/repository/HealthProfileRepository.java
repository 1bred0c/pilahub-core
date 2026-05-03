package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HealthProfileRepository extends JpaRepository<HealthProfile, UUID> {

    /**
     * Find all health profiles by trainee ID, ordered by creation date descending
     */
    @Query("SELECT hp FROM HealthProfile hp WHERE hp.trainee.traineeId = :traineeId ORDER BY hp.createdAt DESC")
    List<HealthProfile> findByTraineeIdOrderByCreatedAtDesc(@Param("traineeId") UUID traineeId);

    /**
     * Find the latest health profile for a trainee
     */
    @Query("SELECT hp FROM HealthProfile hp WHERE hp.trainee.traineeId = :traineeId AND hp.isLatest = true")
    Optional<HealthProfile> findLatestByTraineeId(@Param("traineeId") UUID traineeId);

    /**
     * Check if a health profile exists for a trainee
     */
    @Query("SELECT COUNT(hp) > 0 FROM HealthProfile hp WHERE hp.trainee.traineeId = :traineeId")
    boolean existsByTraineeId(@Param("traineeId") UUID traineeId);
}

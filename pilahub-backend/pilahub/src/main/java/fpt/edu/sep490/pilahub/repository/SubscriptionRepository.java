package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.SubscriptionStatus;
import fpt.edu.sep490.pilahub.pojo.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByTrainee_TraineeId(UUID traineeId);

    List<Subscription> findByTrainee_TraineeIdAndStatus(UUID traineeId, SubscriptionStatus status);

    Optional<Subscription> findFirstByTrainee_TraineeIdAndStatus(UUID traineeId, SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.trainee.traineeId = :traineeId AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByTraineeId(@Param("traineeId") UUID traineeId);

    @Query("SELECT s FROM Subscription s WHERE s.endDate <= :now AND s.status = 'ACTIVE'")
    List<Subscription> findExpiredActiveSubscriptions(@Param("now") Instant now);

    boolean existsByTrainee_TraineeIdAndStatus(UUID traineeId, SubscriptionStatus status);
}

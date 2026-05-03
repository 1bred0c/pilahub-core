package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.FitnessGoal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FitnessGoalRepository extends JpaRepository<FitnessGoal, UUID> {

    List<FitnessGoal> findByActiveTrue();

    Page<FitnessGoal> findAll(Pageable pageable);

    Optional<FitnessGoal> findByCode(String code);

    Optional<FitnessGoal> findByCodeAndActiveTrue(String code);

    List<FitnessGoal> findByVietnameseNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String vietnameseName, String description);

    boolean existsByCode(String code);
}

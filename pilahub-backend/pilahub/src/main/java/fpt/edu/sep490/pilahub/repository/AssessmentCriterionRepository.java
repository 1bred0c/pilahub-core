package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.AssessmentCriterion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AssessmentCriterionRepository extends JpaRepository<AssessmentCriterion, UUID> {

    List<AssessmentCriterion> findByIsActiveTrueOrderByDisplayOrderAsc();

    List<AssessmentCriterion> findAllByOrderByDisplayOrderAsc();

    List<AssessmentCriterion> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByDisplayOrder(Integer displayOrder);
}


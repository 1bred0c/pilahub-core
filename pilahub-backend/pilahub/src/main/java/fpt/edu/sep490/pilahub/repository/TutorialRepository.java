package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Tutorial;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TutorialRepository extends JpaRepository<Tutorial, UUID> {

    Optional<Tutorial> findByExercise(Exercise exercise);

    Optional<Tutorial> findByExercise_ExerciseId(UUID exerciseId);

    List<Tutorial> findByPublishedTrue();

    Optional<Tutorial> findByExercise_ExerciseIdAndPublishedTrue(UUID exerciseId);

    boolean existsByExercise_ExerciseId(UUID exerciseId);

    void deleteByExercise_ExerciseId(UUID exerciseId);
}

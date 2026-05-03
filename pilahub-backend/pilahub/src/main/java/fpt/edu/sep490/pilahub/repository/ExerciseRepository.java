package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, UUID> {

    List<Exercise> findByActiveTrue();

    Optional<Exercise> findByExerciseIdAndActiveTrue(UUID exerciseId);

    List<Exercise> findByNameContainingIgnoreCase(String name);

    List<Exercise> findByDifficultyLevel(DifficultyLevel difficultyLevel);

    List<Exercise> findByDifficultyLevelAndActiveTrue(DifficultyLevel difficultyLevel);

    boolean existsByExerciseId(UUID exerciseId);

    Optional<Exercise> findByNameIgnoreCase(String name);

    @Query("""
    SELECT COUNT(cl) > 0
    FROM CourseLesson cl
    JOIN LessonExercise le ON cl.lesson.lessonId = le.lesson.lessonId
    WHERE cl.course.courseId = :courseId
      AND le.exercise.exerciseId = :exerciseId
""")
    boolean existsCourseExercise(@Param("courseId") UUID courseId,
                                 @Param("exerciseId") UUID exerciseId);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.CourseLessonProgress;
import fpt.edu.sep490.pilahub.pojo.LessonExercise;
import fpt.edu.sep490.pilahub.pojo.LessonExerciseProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonExerciseProgressRepository extends JpaRepository<LessonExerciseProgress, UUID> {

    List<LessonExerciseProgress> findByCourseLessonProgress(CourseLessonProgress courseLessonProgress);

    List<LessonExerciseProgress> findByCourseLessonProgress_ProgressId(UUID courseLessonProgressId);

    List<LessonExerciseProgress> findByCourseLessonProgress_ProgressIdAndCompletedTrue(UUID courseLessonProgressId);

    List<LessonExerciseProgress> findByCourseLessonProgress_ProgressIdAndCompletedFalse(UUID courseLessonProgressId);

    Optional<LessonExerciseProgress> findByCourseLessonProgress_ProgressIdAndLessonExercise_LessonExerciseId(
            UUID courseLessonProgressId, UUID lessonExerciseId);

    boolean existsByCourseLessonProgress_ProgressIdAndLessonExercise_LessonExerciseId(
            UUID courseLessonProgressId, UUID lessonExerciseId);

    long countByCourseLessonProgress_ProgressIdAndCompletedTrue(UUID courseLessonProgressId);

    long countByCourseLessonProgress_ProgressId(UUID courseLessonProgressId);
}


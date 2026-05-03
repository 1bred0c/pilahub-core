package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.LessonExercise;
import fpt.edu.sep490.pilahub.pojo.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonExerciseRepository extends JpaRepository<LessonExercise, UUID> {

    List<LessonExercise> findByLesson(Lesson lesson);

    List<LessonExercise> findByLesson_LessonId(UUID lessonId);

    List<LessonExercise> findByLesson_LessonIdOrderByDisplayOrderAsc(UUID lessonId);

    void deleteByLesson_LessonId(UUID lessonId);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByActiveTrue();

    Optional<Lesson> findByLessonIdAndActiveTrue(UUID lessonId);

    List<Lesson> findByNameContainingIgnoreCase(String name);

    boolean existsByLessonId(UUID lessonId);
}

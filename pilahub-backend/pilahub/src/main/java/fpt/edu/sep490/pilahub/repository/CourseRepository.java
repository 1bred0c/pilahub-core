package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import fpt.edu.sep490.pilahub.pojo.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByActiveTrue();

    Optional<Course> findByCourseIdAndActiveTrue(UUID courseId);

    List<Course> findByNameContainingIgnoreCase(String name);

    List<Course> findByLevel(DifficultyLevel level);

    List<Course> findByLevelAndActiveTrue(DifficultyLevel level);

    boolean existsByCourseId(UUID courseId);
}

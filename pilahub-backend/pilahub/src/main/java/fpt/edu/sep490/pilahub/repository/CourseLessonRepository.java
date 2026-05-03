package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import fpt.edu.sep490.pilahub.pojo.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, UUID> {

    List<CourseLesson> findByCourse(Course course);

    List<CourseLesson> findByCourse_CourseId(UUID courseId);

    List<CourseLesson> findByCourse_CourseIdOrderByDisplayOrderAsc(UUID courseId);

    void deleteByCourse_CourseId(UUID courseId);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import fpt.edu.sep490.pilahub.pojo.CourseLessonProgress;
import fpt.edu.sep490.pilahub.pojo.TraineeCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CourseLessonProgressRepository extends JpaRepository<CourseLessonProgress, UUID> {

    List<CourseLessonProgress> findByTraineeCourse(TraineeCourse traineeCourse);

    List<CourseLessonProgress> findByTraineeCourse_TraineeCourseId(UUID traineeCourseId);

    List<CourseLessonProgress> findByCourseLesson(CourseLesson courseLesson);

    List<CourseLessonProgress> findByCourseLesson_CourseLessonId(UUID courseLessonId);

    Optional<CourseLessonProgress> findByTraineeCourse_TraineeCourseIdAndCourseLesson_CourseLessonId(
            UUID traineeCourseId, UUID courseLessonId);

    List<CourseLessonProgress> findByTraineeCourse_TraineeCourseIdAndCompletedTrue(UUID traineeCourseId);

    List<CourseLessonProgress> findByTraineeCourse_TraineeCourseIdAndCompletedFalse(UUID traineeCourseId);

    List<CourseLessonProgress> findByCompletedTrue();

    List<CourseLessonProgress> findByCompletedFalse();

    List<CourseLessonProgress> findByTraineeCourse_Trainee_TraineeIdAndStartedAtGreaterThanEqualAndStartedAtLessThanOrderByStartedAtAsc(
            UUID traineeId,
            java.time.Instant startDate,
            java.time.Instant endDate);

    boolean existsByTraineeCourse_TraineeCourseIdAndCourseLesson_CourseLessonId(UUID traineeCourseId,
            UUID courseLessonId);

    void deleteByTraineeCourse_TraineeCourseId(UUID traineeCourseId);

    void deleteByCourseLesson_CourseLessonId(UUID courseLessonId);
}

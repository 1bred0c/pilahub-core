package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Course;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.TraineeCourse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TraineeCourseRepository extends JpaRepository<TraineeCourse, UUID> {

    List<TraineeCourse> findByTrainee(Trainee trainee);

    List<TraineeCourse> findByTrainee_TraineeId(UUID traineeId);

    List<TraineeCourse> findByCourse(Course course);

    List<TraineeCourse> findByCourse_CourseId(UUID courseId);

    Optional<TraineeCourse> findByTrainee_TraineeIdAndCourse_CourseId(UUID traineeId, UUID courseId);

    boolean existsByTrainee_TraineeIdAndCourse_CourseId(UUID traineeId, UUID courseId);

    void deleteByTrainee_TraineeId(UUID traineeId);

    void deleteByCourse_CourseId(UUID courseId);
}

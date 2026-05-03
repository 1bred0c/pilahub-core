package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TraineeCourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateTraineeCourseRequest;

import java.util.List;
import java.util.UUID;

public interface TraineeCourseService {

    TraineeCourseDto enrollCourse(CreateTraineeCourseRequest request);

    TraineeCourseDto getById(UUID traineeCourseId);

    List<TraineeCourseDto> getByTraineeId(UUID traineeId);

    TraineeCourseDto updateProgress(UUID traineeCourseId, Integer progressPercentage);

    TraineeCourseDto activateTraineeCourse(UUID traineeCourseId);

    void deleteTraineeCourse(UUID traineeCourseId);

    boolean isEnrolled(UUID traineeId, UUID courseId);
}

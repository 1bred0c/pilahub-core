package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonProgressRequest;

import java.util.List;
import java.util.UUID;

public interface CourseLessonProgressService {

    List<CourseLessonProgressDto> createProgress(CreateCourseLessonProgressRequest scheduleRequest);

    List<CourseLessonProgressDto> resetProgressAndReschedule(CreateCourseLessonProgressRequest scheduleRequest);

    List<CourseLessonProgressDto> rescheduleIncompleteProgress(CreateCourseLessonProgressRequest scheduleRequest);

    CourseLessonProgressDto getById(UUID progressId);

    List<CourseLessonProgressDto> getByTraineeCourseId(UUID traineeCourseId);

    List<CourseLessonProgressDto> getByCourseLessonId(UUID courseLessonId);

    CourseLessonProgressDto getByTraineeCourseIdAndCourseLessonId(UUID traineeCourseId, UUID courseLessonId);

    List<CourseLessonProgressDto> getCompletedByTraineeCourseId(UUID traineeCourseId);

    List<CourseLessonProgressDto> getIncompleteByTraineeCourseId(UUID traineeCourseId);

    CourseLessonProgressDto updateProgress(UUID progressId, UpdateCourseLessonProgressRequest request);

    CourseLessonProgressDto startLesson(UUID progressId);

    CourseLessonProgressDto markAsCompleted(UUID progressId);

    void deleteProgress(UUID progressId);

    void deleteByTraineeCourseId(UUID traineeCourseId);

    void deleteByCourseLessonId(UUID courseLessonId);

    boolean existsByTraineeCourseIdAndCourseLessonId(UUID traineeCourseId, UUID courseLessonId);
}

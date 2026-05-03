package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CourseLessonDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonRequest;

import java.util.List;
import java.util.UUID;

public interface CourseLessonService {

    List<CourseLessonDto> createCourseLesson(UUID CourseId, List<CreateCourseLessonRequest> request);

    CourseLessonDto getById(UUID courseLessonId);

    List<CourseLessonDto> getByCourseId(UUID courseId);

    List<CourseLessonDto> getByCourseIdOrdered(UUID courseId);

    CourseLessonDto updateCourseLesson(UUID courseLessonId, UpdateCourseLessonRequest request);

    void deleteCourseLesson(UUID courseLessonId);

    void deleteByCourseId(UUID courseId);
}

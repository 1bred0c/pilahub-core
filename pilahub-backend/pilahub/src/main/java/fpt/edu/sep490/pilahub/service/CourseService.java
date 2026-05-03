package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseRequest;
import fpt.edu.sep490.pilahub.dto.response.CourseEditDetailsResponse;
import fpt.edu.sep490.pilahub.dto.response.CourseWithDetailsResponse;

import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseDto createCourse(CreateCourseRequest request);

    CourseDto getById(UUID courseId);

    CourseWithDetailsResponse getCourseWithDetails(UUID courseId);

    CourseEditDetailsResponse getCourseEditDetails(UUID courseId);

    List<CourseDto> getAll();

    List<CourseDto> getAllActive();

    List<CourseDto> searchByName(String name);

    List<CourseDto> getByLevel(String level);

    List<CourseDto> getActiveByLevel(String level);

    CourseDto updateCourse(UUID courseId, UpdateCourseRequest request);

    void activateCourse(UUID courseId);

    void deactivateCourse(UUID courseId);

    void deleteCourse(UUID courseId);
}

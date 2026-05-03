package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.LessonDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonRequest;

import java.util.List;
import java.util.UUID;

public interface LessonService {

    LessonDto createLesson(CreateLessonRequest request);

    LessonDto getById(UUID lessonId);

    List<LessonDto> getAll();

    List<LessonDto> searchByName(String name);

    LessonDto updateLesson(UUID lessonId, UpdateLessonRequest request);

    void deactivateLesson(UUID lessonId);

    void deleteLesson(UUID lessonId);
}

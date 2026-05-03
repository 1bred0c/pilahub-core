package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.LessonExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonExerciseRequest;

import java.util.List;
import java.util.UUID;

public interface LessonExerciseService {

    List<LessonExerciseDto> createLessonExercise(UUID lessonId, List<CreateLessonExerciseRequest> request);

    LessonExerciseDto getById(UUID lessonExerciseId);

    List<LessonExerciseDto> getByLessonId(UUID lessonId);

    List<LessonExerciseDto> getByLessonIdOrdered(UUID lessonId);

    LessonExerciseDto updateLessonExercise(UUID lessonExerciseId, UpdateLessonExerciseRequest request);

    void deleteLessonExercise(UUID lessonExerciseId);

    void deleteByLessonId(UUID lessonId);
}

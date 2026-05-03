package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.LessonExerciseProgressDto;

import java.util.List;
import java.util.UUID;

public interface LessonExerciseProgressService {

    List<LessonExerciseProgressDto> getByCourseLessonProgressId(UUID courseLessonProgressId);

    List<LessonExerciseProgressDto> getCompletedByCourseLessonProgressId(UUID courseLessonProgressId);

    List<LessonExerciseProgressDto> getIncompleteByCourseLessonProgressId(UUID courseLessonProgressId);

    LessonExerciseProgressDto getById(UUID lessonExerciseProgressId);
}


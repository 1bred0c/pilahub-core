package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.LessonExerciseProgressDto;
import fpt.edu.sep490.pilahub.pojo.LessonExerciseProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonExerciseProgressMapper {

    @Mapping(target = "courseLessonProgressId", source = "courseLessonProgress.progressId")
    @Mapping(target = "lessonExerciseId", source = "lessonExercise.lessonExerciseId")
    @Mapping(target = "exerciseId", source = "lessonExercise.exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "lessonExercise.exercise.name")
    LessonExerciseProgressDto toDto(LessonExerciseProgress lessonExerciseProgress);
}


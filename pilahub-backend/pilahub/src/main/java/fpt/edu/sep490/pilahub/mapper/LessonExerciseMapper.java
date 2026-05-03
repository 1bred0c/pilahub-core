package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.LessonExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonExerciseRequest;
import fpt.edu.sep490.pilahub.pojo.LessonExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonExerciseMapper {

    @Mapping(target = "lessonId", source = "lesson.lessonId")
    @Mapping(target = "lessonName", source = "lesson.name")
    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    LessonExerciseDto toDto(LessonExercise lessonExercise);

    @Mapping(target = "lessonExerciseId", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    LessonExercise toEntity(CreateLessonExerciseRequest request);

    @Mapping(target = "lessonExerciseId", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateLessonExerciseRequest request, @MappingTarget LessonExercise lessonExercise);
}

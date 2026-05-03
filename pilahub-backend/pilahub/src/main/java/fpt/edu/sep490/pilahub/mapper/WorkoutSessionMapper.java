package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.WorkoutSessionDto;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WorkoutSessionMapper {

    @Mapping(target = "traineeId", source = "trainee.traineeId")
    @Mapping(target = "personalExerciseId", source = "personalExercise.personalExerciseId")
    @Mapping(target = "lessonExerciseProgressId", source = "lessonExerciseProgress.lessonExerciseProgressId")
    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    WorkoutSessionDto toDto(WorkoutSession workoutSession);
}



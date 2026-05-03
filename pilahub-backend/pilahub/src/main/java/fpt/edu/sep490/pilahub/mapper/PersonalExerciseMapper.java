package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.pojo.PersonalExercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonalExerciseMapper {

    @Mapping(target = "personalScheduleId", source = "personalSchedule.personalScheduleId")
    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "exerciseName", source = "exercise.name")
    @Mapping(target = "imageUrl", source = "exercise.imageUrl")
    @Mapping(target = "haveAIsupported", source = "exercise.haveAIsupported")
    @Mapping(target = "nameInModelAI", source = "exercise.nameInModelAI")
    PersonalExerciseDto toDto(PersonalExercise personalExercise);
}

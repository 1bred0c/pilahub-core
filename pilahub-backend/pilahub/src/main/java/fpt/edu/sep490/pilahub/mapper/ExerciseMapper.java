package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseRequest;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = {
        BodyPartMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExerciseMapper {

    @Mapping(target = "havePracticed", source = "havePracticed")
    ExerciseDto toDto(Exercise exercise, boolean havePracticed);

    default ExerciseDto toDto(Exercise exercise) {
        return toDto(exercise, false);
    }

    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bodyParts", ignore = true)
    Exercise toEntity(CreateExerciseRequest request);

    @Mapping(target = "exerciseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "bodyParts", ignore = true)
    void updateEntityFromRequest(UpdateExerciseRequest request, @MappingTarget Exercise exercise);
}

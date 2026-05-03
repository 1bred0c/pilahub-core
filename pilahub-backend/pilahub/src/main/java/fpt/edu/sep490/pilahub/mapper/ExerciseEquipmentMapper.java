package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ExerciseEquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.pojo.ExerciseEquipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExerciseEquipmentMapper {

    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    @Mapping(target = "equipmentId", source = "equipment.equipmentId")
    @Mapping(target = "equipmentName", source = "equipment.name")
    ExerciseEquipmentDto toDto(ExerciseEquipment exerciseEquipment);

    @Mapping(target = "exerciseEquipmentId", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ExerciseEquipment toEntity(CreateExerciseEquipmentRequest request);

    @Mapping(target = "exerciseEquipmentId", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "equipment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateExerciseEquipmentRequest request, @MappingTarget ExerciseEquipment exerciseEquipment);
}

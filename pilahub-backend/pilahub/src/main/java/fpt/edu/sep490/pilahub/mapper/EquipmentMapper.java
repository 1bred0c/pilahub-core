package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.EquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateEquipmentRequest;
import fpt.edu.sep490.pilahub.pojo.Equipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EquipmentMapper {

    EquipmentDto toDto(Equipment equipment);
    List<EquipmentDto> toDto(List<Equipment> equipments);

    @Mapping(target = "equipmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Equipment toEntity(CreateEquipmentRequest request);

    @Mapping(target = "equipmentId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateEquipmentRequest request, @MappingTarget Equipment equipment);
}

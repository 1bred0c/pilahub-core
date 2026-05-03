package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.BodyPartDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateBodyPartRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateBodyPartRequest;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface BodyPartMapper {

    BodyPartDto toDto(BodyPart bodyPart);

    List<BodyPartDto> toDto(List<BodyPart> bodyParts);

    @Mapping(target = "exercises", ignore = true)
    @Mapping(target = "bodyPartId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BodyPart toEntity(BodyPartDto bodyPartDto);

    @Mapping(target = "exercises", ignore = true)
    @Mapping(target = "bodyPartId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    BodyPart toEntity(CreateBodyPartRequest request);

    @Mapping(target = "exercises", ignore = true)
    @Mapping(target = "bodyPartId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateBodyPartRequest request, @MappingTarget BodyPart bodyPart);
}

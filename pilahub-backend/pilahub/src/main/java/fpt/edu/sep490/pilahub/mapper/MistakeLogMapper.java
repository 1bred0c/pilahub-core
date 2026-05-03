package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.MistakeLogDto;
import fpt.edu.sep490.pilahub.pojo.MistakeLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MistakeLogMapper {

    @Mapping(target = "workoutSessionId", source = "workoutSession.workoutSessionId")
    @Mapping(target = "bodyPartId", source = "bodyPart.bodyPartId")
    @Mapping(target = "bodyPartName", source = "bodyPart.name")
    MistakeLogDto toDto(MistakeLog mistakeLog);
}


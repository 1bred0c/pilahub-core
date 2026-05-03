package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PersonalStageDto;
import fpt.edu.sep490.pilahub.pojo.PersonalStage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonalStageMapper {

    @Mapping(target = "roadmapId", source = "roadmap.roadmapId")
    @Mapping(target = "stageId", source = "stage.stageId")
    PersonalStageDto toDto(PersonalStage stage);
}

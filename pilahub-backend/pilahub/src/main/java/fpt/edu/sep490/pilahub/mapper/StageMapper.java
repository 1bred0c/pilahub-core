package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.StageDto;
import fpt.edu.sep490.pilahub.dto.request.CreateStageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateStageRequest;
import fpt.edu.sep490.pilahub.pojo.Stage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface StageMapper {

    StageDto toDto(Stage stage);

    @Mapping(target = "stageId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Stage toEntity(CreateStageRequest request);

    @Mapping(target = "stageId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateStageRequest request, @MappingTarget Stage stage);
}

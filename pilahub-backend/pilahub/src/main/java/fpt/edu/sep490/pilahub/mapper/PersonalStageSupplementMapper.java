package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PersonalStageSupplementDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.pojo.PersonalStageSupplement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonalStageSupplementMapper {

    @Mapping(target = "personalStageId", source = "personalStage.personalStageId")
    @Mapping(target = "supplementId", source = "supplement.supplementId")
    @Mapping(target = "supplementName", source = "supplement.name")
    @Mapping(target = "supplementImageUrl", source = "supplement.imageUrl")
    PersonalStageSupplementDto toDto(PersonalStageSupplement personalStageSupplement);

    @Mapping(target = "personalStageSupplementId", ignore = true)
    @Mapping(target = "personalStage", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PersonalStageSupplement toEntity(CreatePersonalStageSupplementRequest request);

    @Mapping(target = "personalStageSupplementId", ignore = true)
    @Mapping(target = "personalStage", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdatePersonalStageSupplementRequest request, @MappingTarget PersonalStageSupplement personalStageSupplement);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.SupplementPurposeDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.pojo.SupplementPurpose;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {PurposeMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplementPurposeMapper {

    @Mapping(target = "supplementId", source = "supplement.supplementId")
    SupplementPurposeDto toDto(SupplementPurpose supplementPurpose);

    @Mapping(target = "supplementPurposeId", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    SupplementPurpose toEntity(CreateSupplementPurposeRequest request);

    @Mapping(target = "supplementPurposeId", ignore = true)
    @Mapping(target = "supplement", ignore = true)
    @Mapping(target = "purpose", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget SupplementPurpose supplementPurpose, UpdateSupplementPurposeRequest request);
}

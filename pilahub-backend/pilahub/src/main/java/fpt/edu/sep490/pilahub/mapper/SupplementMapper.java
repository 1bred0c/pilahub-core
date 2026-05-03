package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.SupplementDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementRequest;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SupplementMapper {

    SupplementDto toDto(Supplement supplement);

    @Mapping(target = "supplementId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Supplement toEntity(CreateSupplementRequest request);

    @Mapping(target = "supplementId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Supplement supplement, UpdateSupplementRequest request);
}

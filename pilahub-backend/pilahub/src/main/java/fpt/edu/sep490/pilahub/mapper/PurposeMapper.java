package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PurposeDto;
import fpt.edu.sep490.pilahub.dto.request.purpose.CreatePurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.purpose.UpdatePurposeRequest;
import fpt.edu.sep490.pilahub.pojo.Purpose;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PurposeMapper {

    PurposeDto toDto(Purpose purpose);

    @Mapping(target = "purposeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Purpose toEntity(CreatePurposeRequest request);

    @Mapping(target = "purposeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget Purpose purpose, UpdatePurposeRequest request);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PackageDto;
import fpt.edu.sep490.pilahub.pojo.Package;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PackageMapper {

    PackageDto toDto(Package packageEntity);
}

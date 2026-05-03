package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.AddressDto;
import fpt.edu.sep490.pilahub.pojo.Address;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AddressMapper {

    AddressDto toDto(Address address);
}

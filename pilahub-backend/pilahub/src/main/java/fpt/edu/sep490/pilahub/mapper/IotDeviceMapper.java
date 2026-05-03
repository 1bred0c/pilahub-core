package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.IotDeviceDto;
import fpt.edu.sep490.pilahub.pojo.IotDevice;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface IotDeviceMapper {

    IotDeviceDto toDto(IotDevice iotDevice);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {OrderDetailMapper.class})
public interface ShipmentMapper {

    @Mapping(target = "orderId",    source = "order.orderId")
    @Mapping(target = "vendorId",   source = "vendor.vendorId")
    @Mapping(target = "vendorName", source = "vendor.businessName")
    ShipmentDto toDto(Shipment shipment);

    List<ShipmentDto> toDtoList(List<Shipment> shipments);
}

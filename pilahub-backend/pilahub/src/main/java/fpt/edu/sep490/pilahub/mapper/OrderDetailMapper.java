package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.OrderDetailDto;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderDetailMapper {

    @Mapping(target = "orderId",    source = "order.orderId")
    @Mapping(target = "productId",  source = "product.productId")
    @Mapping(target = "shipmentId", source = "shipment.shipmentId")
    OrderDetailDto toDto(OrderDetail orderDetail);

    List<OrderDetailDto> toDtoList(List<OrderDetail> orderDetails);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.OrderDto;
import fpt.edu.sep490.pilahub.dto.request.order.UpdateOrderRequest;
import fpt.edu.sep490.pilahub.pojo.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE, uses = {
        OrderDetailMapper.class, ShipmentMapper.class })
public interface OrderMapper {

    @Mapping(target = "accountId", source = "account.accountId")
    OrderDto toDto(Order order);

    List<OrderDto> toDtoList(List<Order> orders);

    @Mapping(target = "orderId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "totalAmount", ignore = true)
    @Mapping(target = "discountAmount", ignore = true)
    @Mapping(target = "shippingFee", ignore = true)
    @Mapping(target = "orderNumber", ignore = true)
    @Mapping(target = "paid", ignore = true)
    @Mapping(target = "paidAt", ignore = true)
    @Mapping(target = "cancelledAt", ignore = true)
    @Mapping(target = "cancellationReason", ignore = true)
    @Mapping(target = "orderDetails", ignore = true)
    @Mapping(target = "shipments", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateOrderRequest request, @MappingTarget Order order);
}

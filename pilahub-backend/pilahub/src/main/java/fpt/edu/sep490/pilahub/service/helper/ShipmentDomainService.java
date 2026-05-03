package fpt.edu.sep490.pilahub.service.helper;

import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ShipmentDomainService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public Shipment loadShipment(UUID shipmentId) {
        return shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));
    }

    public void syncOrderStatusWithShipment(Shipment shipment) {
        Order order = shipment.getOrder();
        if (order == null) {
            return;
        }

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.REFUNDED) {
            return;
        }

        OrderStatus targetStatus = switch (shipment.getStatus()) {
            case DRAFT -> OrderStatus.CONFIRMED;
            case READY_TO_PICK, PICKING, STORING, TRANSPORTING -> OrderStatus.READY;
            case PICKED, DELIVERING -> OrderStatus.SHIPPED;
            case DELIVERED -> OrderStatus.DELIVERED;
            case DELIVERY_FAIL -> OrderStatus.FAILED_DELIVERY;
            case RETURN, RETURNING, RETURNED -> OrderStatus.RETURNED;
            case CANCELLED -> OrderStatus.CANCELLED;
        };

        boolean shouldMarkPaid = shipment.getStatus() == ShipmentStatus.DELIVERED && !order.isPaid();
        if (shouldMarkPaid) {
            order.setPaid(true);
            if (order.getPaidAt() == null) {
                order.setPaidAt(Instant.now());
            }
        }

        if (order.getStatus() != targetStatus || shouldMarkPaid) {
            order.setStatus(targetStatus);
            orderRepository.save(order);
        }
    }

    /** Propagate a single status to every OrderDetail in the shipment. */
    public void propagateStatusToDetails(Shipment shipment, OrderDetailStatus status) {
        if (shipment.getOrderDetails() == null)
            return;
        for (OrderDetail od : shipment.getOrderDetails()) {
            od.setStatus(status);
        }
        orderDetailRepository.saveAll(shipment.getOrderDetails());
    }
}

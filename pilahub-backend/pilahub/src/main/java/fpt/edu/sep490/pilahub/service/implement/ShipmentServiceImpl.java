package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ShipmentDto;
import fpt.edu.sep490.pilahub.dto.request.shipment.CancelShipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.CreateShipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentFeeRequest;
import fpt.edu.sep490.pilahub.dto.request.shipment.UpdateShipmentTrackingRequest;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ShipmentMapper;
import fpt.edu.sep490.pilahub.pojo.Order;
import fpt.edu.sep490.pilahub.pojo.OrderDetail;
import fpt.edu.sep490.pilahub.pojo.Shipment;
import fpt.edu.sep490.pilahub.repository.OrderDetailRepository;
import fpt.edu.sep490.pilahub.repository.OrderRepository;
import fpt.edu.sep490.pilahub.repository.ShipmentRepository;
import fpt.edu.sep490.pilahub.service.GhnService;
import fpt.edu.sep490.pilahub.service.ShipmentService;
import fpt.edu.sep490.pilahub.service.ShippingProviderService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.service.helper.ShipmentDomainService;
import fpt.edu.sep490.pilahub.service.helper.ShippingServiceFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentDomainService shipmentDomainService;
    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final ShipmentMapper shipmentMapper;
    private final SystemConfigService systemConfigService;
    private final ShippingServiceFactory shippingServiceFactory;
    private final GhnService ghnService;

    // ========== READ ==========

    @Override
    public ShipmentDto getShipmentById(UUID shipmentId) {
        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);
        return shipmentMapper.toDto(shipment);
    }

    @Override
    public List<ShipmentDto> getShipmentsByOrderId(UUID orderId) {
        return shipmentMapper.toDtoList(
                shipmentRepository.findByOrder_OrderId(orderId));
    }

    @Override
    public List<ShipmentDto> getShipmentsByVendorId(UUID vendorId) {
        return shipmentMapper.toDtoList(
                shipmentRepository.findByVendor_VendorId(vendorId));
    }

    @Override
    public List<ShipmentDto> getShipmentsByOrderAndVendor(UUID orderId, UUID vendorId) {
        return shipmentMapper.toDtoList(
                shipmentRepository.findByOrder_OrderIdAndVendor_VendorId(orderId, vendorId));
    }

    @Override
    public List<ShipmentDto> getShipmentsByStatus(ShipmentStatus status) {
        return shipmentMapper.toDtoList(shipmentRepository.findByStatus(status));
    }

    // ========== CREATION ==========

    @Override
    public ShipmentDto createShipmentForOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be CONFIRMED to create shipment");
        }

        if (order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.REFUNDED
                || order.getStatus() == OrderStatus.RETURNED) {
            throw new IllegalStateException(
                    "Cannot create shipments for an order in status: " + order.getStatus());
        }

        List<Shipment> existingShipments = shipmentRepository.findByOrder_OrderId(orderId);
        if (existingShipments.size() > 1) {
            throw new IllegalStateException("Order already has multiple shipments. Expected one shipment per order");
        }

        List<OrderDetail> allDetails = orderDetailRepository.findByOrder_OrderId(orderId);
        if (allDetails.isEmpty()) {
            throw new IllegalStateException("Order has no items");
        }

        UUID vendorId = null;
        for (OrderDetail od : allDetails) {
            if (od.getProduct() == null || od.getProduct().getVendor() == null
                    || od.getProduct().getVendor().getVendorId() == null) {
                throw new IllegalStateException("Order detail missing vendor information");
            }

            UUID detailVendorId = od.getProduct().getVendor().getVendorId();
            if (vendorId == null) {
                vendorId = detailVendorId;
            } else if (!vendorId.equals(detailVendorId)) {
                throw new IllegalStateException("Order items must belong to one vendor");
            }
        }

        // if (vendorId == null) {
        // if (!existingShipments.isEmpty()) {
        // return shipmentMapper.toDtoList(existingShipments);
        // }
        // throw new IllegalStateException("Cannot create shipment: order items are
        // missing vendor information");
        // }

        Shipment saved = existingShipments.isEmpty() ? null : existingShipments.get(0);
        if (saved == null) {
            UUID finalVendorId = vendorId;
            OrderDetail firstVendorDetail = allDetails.stream()
                    .filter(od -> od.getProduct() != null
                            && od.getProduct().getVendor() != null
                            && (finalVendorId).equals(od.getProduct().getVendor().getVendorId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Cannot resolve vendor for shipment creation"));

            saved = Shipment.builder()
                    .order(order)
                    .vendor(firstVendorDetail.getProduct().getVendor())
                    .status(ShipmentStatus.DRAFT)
                    .build();
            saved = shipmentRepository.save(saved);
            log.info("Created shipment {} for order {} / vendor {}", saved.getShipmentId(), orderId, vendorId);
        } else if (saved.getVendor() != null && saved.getVendor().getVendorId() != null
                && !vendorId.equals(saved.getVendor().getVendorId())) {
            throw new IllegalStateException("Existing shipment vendor does not match order items vendor");
        }

        List<OrderDetail> toAssign = new ArrayList<>();
        for (OrderDetail od : allDetails) {
            if (od.getShipment() == null) {
                od.setShipment(saved);
                toAssign.add(od);
            }
        }
        if (!toAssign.isEmpty()) {
            orderDetailRepository.saveAll(toAssign);
            log.info("Assigned {} order detail(s) to shipment {} for order {}",
                    toAssign.size(), saved.getShipmentId(), orderId);
        }

        return shipmentMapper.toDto(saved);
    }

    @Override
    public ShipmentDto createShippingProvider(UUID shipmentId, CreateShipmentRequest request) {

        if (request == null || request.shippingProvider() == null) {
            throw new IllegalArgumentException("shippingProvider is required");
        }

        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);

        if (shipment.getOrder() == null
                || shipment.getOrder().getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Order must be CONFIRMED");
        }

        if (shipment.getStatus() != ShipmentStatus.DRAFT) {
            throw new IllegalStateException("Shipment already processed");
        }

        ShippingProvider provider = request.shippingProvider();

        ShippingProviderService service = shippingServiceFactory.get(provider);

        Shipment processed = service.createProviderForShipment(shipment, request);
        Shipment saved;
        try {
            saved = shipmentRepository.save(processed);
        } catch (RuntimeException saveException) {
            if (provider == ShippingProvider.GHN) {
                compensateGhnOrderIfNeeded(processed, shipmentId, saveException);
            }
            throw saveException;
        }

        return shipmentMapper.toDto(saved);
    }

    private void compensateGhnOrderIfNeeded(Shipment processed, UUID shipmentId, RuntimeException saveException) {
        if (processed == null || processed.getTrackingNumber() == null || processed.getTrackingNumber().isBlank()) {
            return;
        }

        try {
            ghnService.cancelShipmentOrderCompensation(
                    processed,
                    "Compensation: local save failed after GHN order creation for shipment " + shipmentId);
            log.warn("Compensated GHN order {} because local save failed for shipment {}",
                    processed.getTrackingNumber(), shipmentId);
        } catch (RuntimeException compensationException) {
            saveException.addSuppressed(compensationException);
            log.error("Failed compensation cancel for GHN order {} after save failure on shipment {}",
                    processed.getTrackingNumber(), shipmentId, compensationException);
        }
    }

    // ========== STATUS ==========

    @Override
    public ShipmentDto updateShipmentStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);
        Instant now = Instant.now();

        shipment.setStatus(newStatus);

        switch (newStatus) {
            case PICKED -> {
                shipment.setShippedAt(now);
                shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.SHIPPED);
            }
            case DELIVERING -> {
                shipment.setShippedAt(now);
                shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.SHIPPED);
            }
            case DELIVERED -> {
                shipment.setDeliveredAt(now);
                int holdingDays = (shipment.getVendor() != null
                        && shipment.getVendor().getHoldingDays() != null)
                                ? shipment.getVendor().getHoldingDays()
                                : systemConfigService.getDefaultHoldingDays();
                shipment.setReturnDeadline(now.plus(holdingDays, ChronoUnit.DAYS));
                shipment.setPayoutReleaseDate(now.plus(holdingDays, ChronoUnit.DAYS));
                shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.DELIVERED);
                shipmentDomainService.syncOrderStatusWithShipment(shipment);
                Shipment saved = shipmentRepository.save(shipment);
                completeOrderWhenAllShipmentsDelivered(shipment.getOrder());
                log.info("Shipment {} marked as DELIVERED", shipmentId);
                return shipmentMapper.toDto(saved);
            }
            case CANCELLED -> {
                shipment.setCancelledAt(now);
                shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.CANCELLED);
                log.info("Shipment {} cancelled via status update", shipmentId);
            }
            default -> {
                /* DELIVERY_FAIL, RETURN, RETURNING, RETURNED — no propagation */ }
        }

        shipmentDomainService.syncOrderStatusWithShipment(shipment);

        return shipmentMapper.toDto(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentDto cancelShipment(UUID shipmentId, CancelShipmentRequest request) {
        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);

        if (shipment.getStatus() == ShipmentStatus.DELIVERED) {
            throw new IllegalStateException("Cannot cancel a shipment that has already been delivered");
        }
        if (shipment.getStatus() == ShipmentStatus.CANCELLED) {
            throw new IllegalStateException("Shipment is already cancelled");
        }

        shipment.setStatus(ShipmentStatus.CANCELLED);
        shipment.setCancelledAt(Instant.now());
        shipment.setCancellationReason(request.cancellationReason());

        shipmentDomainService.propagateStatusToDetails(shipment, OrderDetailStatus.CANCELLED);
        shipmentDomainService.syncOrderStatusWithShipment(shipment);

        log.info("Shipment {} cancelled. Reason: {}", shipmentId, request.cancellationReason());
        return shipmentMapper.toDto(shipmentRepository.save(shipment));
    }

    // ========== UPDATE ==========

    @Override
    public ShipmentDto updateTracking(UUID shipmentId, UpdateShipmentTrackingRequest request) {
        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);

        if (request.shippingProvider() != null) {
            shipment.setShippingProvider(request.shippingProvider());
        }
        if (request.trackingNumber() != null) {
            shipment.setTrackingNumber(request.trackingNumber());
        }

        log.info("Shipment {} tracking updated: provider={}, tracking={}", shipmentId,
                request.shippingProvider(), request.trackingNumber());
        return shipmentMapper.toDto(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentDto updateShippingFee(UUID shipmentId, UpdateShipmentFeeRequest request) {
        Shipment shipment = shipmentDomainService.loadShipment(shipmentId);
        Order order = shipment.getOrder();
        order.setShippingFee(request.shippingFee());
        recalculateOrderTotal(order);

        log.info("Shipment {} shipping fee updated to {} (persisted on order {})",
                shipmentId, request.shippingFee(), order.getOrderId());
        return shipmentMapper.toDto(shipmentRepository.save(shipment));
    }

    // ========== PRIVATE HELPERS ==========



    private void completeOrderWhenAllShipmentsDelivered(Order order) {
        if (order == null || order.getOrderId() == null) {
            return;
        }

        if (order.getStatus() == OrderStatus.COMPLETED
                || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.REFUNDED
                || order.getStatus() == OrderStatus.RETURNED) {
            return;
        }

        List<Shipment> shipments = shipmentRepository.findByOrder_OrderId(order.getOrderId());
        if (shipments.isEmpty()) {
            return;
        }

        boolean allDelivered = shipments.stream()
                .allMatch(s -> s.getStatus() == ShipmentStatus.DELIVERED);

        if (allDelivered) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);
            log.info("Order {} auto-completed because all shipments were delivered", order.getOrderId());
        }
    }

    /** Recalculate and persist the parent order's totalAmount. */
    private void recalculateOrderTotal(Order order) {
        BigDecimal subtotal = order.getOrderDetails() == null ? BigDecimal.ZERO
                : order.getOrderDetails().stream()
                        .map(OrderDetail::getSubtotal)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscountAmount() != null ? order.getDiscountAmount() : BigDecimal.ZERO;

        order.setTotalAmount(subtotal.add(shippingFee).subtract(discount));
        orderRepository.save(order);
    }
}

package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.OrderDetailDto;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.TransactionType;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.OrderDetailMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.OrderDetailService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final OrderDetailMapper orderDetailMapper;
    private final ShipmentRepository shipmentRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final SystemConfigService systemConfigService;
    private final SystemConfigRepository systemConfigRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetailById(UUID orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));
        return orderDetailMapper.toDto(orderDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDetailDto> getOrderDetailsByOrderId(UUID orderId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(orderId);
        return orderDetailMapper.toDtoList(orderDetails);
    }

    @Override
    @Transactional
    public void deleteOrderDetail(UUID orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        orderDetailRepository.delete(orderDetail);
        log.info("Order detail deleted successfully: {}", orderDetailId);
    }

    @Override
    @Transactional
    public OrderDetailDto updateOrderDetailQuantity(UUID orderDetailId, Integer newQuantity) {
        if (newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        orderDetail.setQuantity(newQuantity);

        // Recalculate subtotal
        BigDecimal newSubtotal = orderDetail.getUnitPrice()
                .multiply(BigDecimal.valueOf(newQuantity))
                .subtract(orderDetail.getDiscountAmount());
        orderDetail.setSubtotal(newSubtotal);

        OrderDetail updatedDetail = orderDetailRepository.save(orderDetail);
        log.info("Order detail quantity updated: {}, new quantity: {}", orderDetailId, newQuantity);

        return orderDetailMapper.toDto(updatedDetail);
    }

    @Override
    @Transactional
    public OrderDetailDto updateOrderDetailDiscount(UUID orderDetailId, BigDecimal discountAmount) {
        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Discount amount must not be negative");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        orderDetail.setDiscountAmount(discountAmount);

        // Recalculate subtotal
        BigDecimal newSubtotal = orderDetail.getUnitPrice()
                .multiply(BigDecimal.valueOf(orderDetail.getQuantity()))
                .subtract(discountAmount);
        orderDetail.setSubtotal(newSubtotal);

        OrderDetail updatedDetail = orderDetailRepository.save(orderDetail);
        log.info("Order detail discount updated: {}, new discount: {}", orderDetailId, discountAmount);

        return orderDetailMapper.toDto(updatedDetail);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderDetailDto getOrderDetailByOrderAndProduct(UUID orderId, UUID productId) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder_OrderId(orderId);

        OrderDetail orderDetail = orderDetails.stream()
                .filter(detail -> detail.getProduct().getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Order detail not found for order: " + orderId + " and product: " + productId));

        return orderDetailMapper.toDto(orderDetail);
    }

    @Override
    @Transactional
    public List<OrderDetailDto> duplicateOrderDetails(UUID sourceOrderId, UUID targetOrderId) {
        Order sourceOrder = orderRepository.findById(sourceOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Source order not found with ID: " + sourceOrderId));

        Order targetOrder = orderRepository.findById(targetOrderId)
                .orElseThrow(() -> new ResourceNotFoundException("Target order not found with ID: " + targetOrderId));

        List<OrderDetail> sourceDetails = orderDetailRepository.findByOrder_OrderId(sourceOrderId);
        List<OrderDetail> duplicatedDetails = new ArrayList<>();

        for (OrderDetail sourceDetail : sourceDetails) {
            OrderDetail duplicatedDetail = OrderDetail.builder()
                    .order(targetOrder)
                    .product(sourceDetail.getProduct())
                    .quantity(sourceDetail.getQuantity())
                    .unitPrice(sourceDetail.getUnitPrice())
                    .subtotal(sourceDetail.getSubtotal())
                    .discountAmount(sourceDetail.getDiscountAmount())
                    .productName(sourceDetail.getProductName())
                    .productImageUrl(sourceDetail.getProductImageUrl())
                    .build();

            duplicatedDetails.add(duplicatedDetail);
        }

        List<OrderDetail> savedDetails = orderDetailRepository.saveAll(duplicatedDetails);
        log.info("Duplicated {} order details from order {} to order {}",
                savedDetails.size(), sourceOrderId, targetOrderId);

        return orderDetailMapper.toDtoList(savedDetails);
    }

    // ========== STATUS MANAGEMENT ==========

    @Override
    @Transactional
    public OrderDetailDto updateOrderDetailStatus(UUID orderDetailId, OrderDetailStatus newStatus) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        Order order = orderDetail.getOrder();

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("Cannot update items of a cancelled order");
        }

        if (order.getStatus() == OrderStatus.COMPLETED) {
            // Only return/refund transitions are allowed after order completion
            if (newStatus != OrderDetailStatus.RETURNED && newStatus != OrderDetailStatus.REFUNDED) {
                throw new IllegalStateException("Can only process RETURNED or REFUNDED for completed order items");
            }
        }

        OrderDetailStatus oldStatus = orderDetail.getStatus();
        orderDetail.setStatus(newStatus);
        Instant now = Instant.now();

        // Shipping lifecycle timestamps are tracked at the Shipment level
        // Shipment shipment = orderDetail.getShipment();
        // switch (newStatus) {
        // case SHIPPED -> {
        // if (shipment != null)
        // shipment.setShippedAt(now);
        // }
        // case DELIVERED -> {
        // if (shipment != null) {
        // shipment.setDeliveredAt(now);
        // int holdingDays = shipment.getVendor() != null
        // && shipment.getVendor().getHoldingDays() != null
        // ? shipment.getVendor().getHoldingDays()
        // : 3;
        // Instant payoutReleaseDate = now.plus(holdingDays, ChronoUnit.DAYS);
        // shipment.setPayoutReleaseDate(payoutReleaseDate);
        // shipment.setReturnDeadline(now.plus(7, ChronoUnit.DAYS));
        // shipmentRepository.save(shipment);
        // log.info("Shipment {} delivered. Payout release: {}, Return deadline: {}",
        // shipment.getShipmentId(), payoutReleaseDate, shipment.getReturnDeadline());
        // }
        // }
        // case COMPLETED -> log.info("OrderDetail {} completed", orderDetailId);
        // case CANCELLED -> log.info("OrderDetail {} cancelled", orderDetailId);
        // case RETURNED -> log.info("OrderDetail {} returned", orderDetailId);
        // case REFUNDED -> log.info("OrderDetail {} refunded", orderDetailId);
        // default -> {
        // /* no extra timestamps for other statuses */ }
        // }

        orderDetailRepository.save(orderDetail);
        log.info("OrderDetail {} status updated from {} to {}", orderDetailId, oldStatus, newStatus);

        // When COMPLETED: check if all items in order are completed → complete the
        // order
        if (newStatus == OrderDetailStatus.COMPLETED) {
            checkAndCompleteOrder(order);
        }

        // When CANCELLED: check if all non-cancelled items are delivered → complete the
        // order
        // or if now ALL items are cancelled → cancel the order
        if (newStatus == OrderDetailStatus.CANCELLED) {
            checkOrderStatusAfterItemCancellation(order);
        }

        return orderDetailMapper.toDto(orderDetail);
    }

    @Override
    @Transactional
    public OrderDetailDto requestReturn(UUID orderDetailId, String reason) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        if (orderDetail.getStatus() != OrderDetailStatus.DELIVERED) {
            throw new IllegalStateException("Only DELIVERED items can be returned");
        }

        // Check return deadline at the Shipment level
        Shipment shipment = orderDetail.getShipment();
        if (shipment != null && shipment.getReturnDeadline() != null
                && Instant.now().isAfter(shipment.getReturnDeadline())) {
            throw new IllegalStateException("Return deadline has passed for this shipment");
        }

        orderDetail.setStatus(OrderDetailStatus.RETURNED);
        OrderDetail saved = orderDetailRepository.save(orderDetail);
        log.info("Return requested for OrderDetail {}: {}", orderDetailId, reason);
        return orderDetailMapper.toDto(saved);
    }

    @Override
    @Transactional
    public OrderDetailDto processRefund(UUID orderDetailId) {
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new ResourceNotFoundException("Order detail not found with ID: " + orderDetailId));

        OrderDetailStatus status = orderDetail.getStatus();
        if (status != OrderDetailStatus.RETURNED && status != OrderDetailStatus.CANCELLED) {
            throw new IllegalStateException("Refund can only be processed for RETURNED or CANCELLED items");
        }

        Order order = orderDetail.getOrder();
        UUID accountId = order.getAccount().getAccountId();

        BigDecimal refundAmount = orderDetail.getSubtotal();

        Wallet customerWallet = walletRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for account: " + accountId));

        customerWallet.setAvailableVND(customerWallet.getAvailableVND().add(refundAmount));
        customerWallet.setBalanceVND(customerWallet.getBalanceVND().add(refundAmount));
        walletRepository.save(customerWallet);

        Transaction refundTx = Transaction.builder()
                .transactionType(TransactionType.REFUND)
                .amount(refundAmount)
                .accountId(accountId)
                .referenceId(orderDetail.getOrderDetailId())
                .description(String.format("Refund for item '%s' in order %s",
                        orderDetail.getProductName(), order.getOrderNumber()))
                .build();
        transactionRepository.save(refundTx);

        orderDetail.setStatus(OrderDetailStatus.REFUNDED);
        OrderDetail saved = orderDetailRepository.save(orderDetail);
        log.info("Refund of {} VND processed for OrderDetail {} (account {})",
                refundAmount, orderDetailId, accountId);

        return orderDetailMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void autoCompleteDeliveredOrderDetailsPastDeadline() {
        Instant now = Instant.now();
        List<OrderDetail> dueDetails = orderDetailRepository
                .findByStatusAndShipment_ReturnDeadlineLessThan(OrderDetailStatus.DELIVERED, now);

        if (dueDetails.isEmpty()) {
            return;
        }

        for (OrderDetail detail : dueDetails) {
            updateOrderDetailStatus(detail.getOrderDetailId(), OrderDetailStatus.COMPLETED);
        }

        log.info("Auto-completed {} order details after return deadline", dueDetails.size());
    }

    // ========== PRIVATE HELPERS ==========

    /**
     * Completes the parent order when all items reach a terminal state that counts
     * as completed (i.e. every item is COMPLETED).
     */
    private void checkAndCompleteOrder(Order order) {
        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            return;
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
        boolean allCompleted = !details.isEmpty() &&
                details.stream().allMatch(d -> d.getStatus() == OrderDetailStatus.COMPLETED);

        if (allCompleted) {
            order.setStatus(OrderStatus.COMPLETED);
            orderRepository.save(order);

            Vendor vendor = order.getShipments().get(0).getVendor();
            if (vendor == null) {
                throw new IllegalStateException("Order shipment has no vendor");
            }

            double feeRate = vendor.getPlatformFeePercentage() != null
                    ? vendor.getPlatformFeePercentage()
                    : systemConfigService.getDefaultPlatformFeePercentage();

            BigDecimal orderGross = order.getTotalAmount();
            BigDecimal orderFee = orderGross.multiply(BigDecimal.valueOf(feeRate / 100.0));
            BigDecimal orderNet = orderGross.subtract(orderFee);

            String emailAdmin = systemConfigService.getEmailAdmin();
            UUID adminId = accountRepository.findByEmail(emailAdmin)
                    .map(Account::getAccountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));

            transactionRepository.save(Transaction.builder()
                    .transactionType(TransactionType.VENDOR_EARNING)
                    .amount(orderNet)
                    .accountId(adminId)
                    .referenceId(order.getOrderId())
                    .description(String.format(
                            "Shop được hưởng từ đơn hàng | Đơn hàng: %s | %s VND",
                            order.getOrderNumber(),
                            orderNet))
                    .build());

            transactionRepository.save(Transaction.builder()
                    .transactionType(TransactionType.PLATFORM_FEE)
                    .amount(orderFee)
                    .accountId(adminId)
                    .referenceId(order.getOrderId())
                    .description(String.format(
                            "Phí nền tảng | Đơn hàng: %s | %s VND",
                            order.getOrderNumber(),
                            orderFee))
                    .build());

            TransactionType type = TransactionType.SHIPPING_FEE_VENDOR;

            if (order.getShipments().get(0).getShippingProvider() != ShippingProvider.SELF) {
                type = TransactionType.SHIPPING_FEE_THIRD_PARTY;
            }

            transactionRepository.save(Transaction.builder()
                    .transactionType(type)
                    .amount(order.getShippingFee())
                    .accountId(adminId)
                    .referenceId(order.getOrderId())
                    .description(String.format(
                            "Phí giao hàng | Đơn hàng: %s | %s VND",
                            order.getOrderNumber(),
                            order.getShippingFee()))
                    .build());

            log.info("Order {} automatically completed - all items completed", order.getOrderNumber());
        }
    }

    /**
     * After an individual item is cancelled, decide whether to cancel the whole
     * order
     * (all items cancelled) or do nothing (remaining active items exist).
     */
    private void checkOrderStatusAfterItemCancellation(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }

        List<OrderDetail> details = orderDetailRepository.findByOrder_OrderId(order.getOrderId());
        boolean allCancelled = details.stream()
                .allMatch(d -> d.getStatus() == OrderDetailStatus.CANCELLED);

        if (allCancelled) {
            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelledAt(Instant.now());
            orderRepository.save(order);
            log.info("Order {} automatically cancelled - all items cancelled", order.getOrderNumber());
        }
    }
}

package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.OrderDetailDto;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface OrderDetailService {

    /**
     * Get order detail by ID
     */
    OrderDetailDto getOrderDetailById(UUID orderDetailId);

    /**
     * Get all order details for a specific order
     */
    List<OrderDetailDto> getOrderDetailsByOrderId(UUID orderId);

    /**
     * Delete order detail (Admin only)
     */
    void deleteOrderDetail(UUID orderDetailId);

    /**
     * Update order detail quantity
     */
    OrderDetailDto updateOrderDetailQuantity(UUID orderDetailId, Integer newQuantity);

    /**
     * Update order detail discount
     */
    OrderDetailDto updateOrderDetailDiscount(UUID orderDetailId, BigDecimal discountAmount);

    /**
     * Get order detail by order and product
     */
    OrderDetailDto getOrderDetailByOrderAndProduct(UUID orderId, UUID productId);

    /**
     * Duplicate order details from one order to another
     */
    List<OrderDetailDto> duplicateOrderDetails(UUID sourceOrderId, UUID targetOrderId);

    /**
     * Update order detail status with business logic:
     * - Sets timestamps (shippedAt, deliveredAt) based on status
     * - Calculates payoutReleaseDate using vendor's holdingDays when DELIVERED
     * - Auto-completes parent order when all items are COMPLETED
     * - Handles RETURNED / REFUNDED transitions
     */
    OrderDetailDto updateOrderDetailStatus(UUID orderDetailId, OrderDetailStatus newStatus);

    /**
     * Request a return for a delivered order detail (must be within return
     * deadline)
     */
    OrderDetailDto requestReturn(UUID orderDetailId, String reason);

    /**
     * Process refund for an order detail that has been returned or cancelled
     * Credits customer wallet with the item subtotal
     */
    OrderDetailDto processRefund(UUID orderDetailId);

    /**
     * Auto-complete delivered order details once shipment return deadline has
     * passed.
     */
    void autoCompleteDeliveredOrderDetailsPastDeadline();
}

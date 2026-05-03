package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.OrderDto;
import fpt.edu.sep490.pilahub.dto.request.order.CancelOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.CreateOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.OrderItemRequest;
import fpt.edu.sep490.pilahub.dto.request.order.SearchOrderRequest;
import fpt.edu.sep490.pilahub.dto.request.order.UpdateOrderRequest;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface OrderService {

    // ========== 1. CORE CRUD OPERATIONS ==========

    /**
     * Place an order for the given account.
     * Items are automatically grouped by vendor: one {@link OrderDto} is created
     * per vendor present in the request, each with its own Shipment.
     * The customer's wallet is charged once for the combined total.
     *
     * @return list of created orders — one per vendor
     */
    List<OrderDto> createOrder(UUID accountId, CreateOrderRequest request);

    /**
     * Get order by ID
     */
    OrderDto getOrderById(UUID orderId);

    /**
     * Get order by order number
     */
    OrderDto getOrderByOrderNumber(String orderNumber);

    /**
     * Get all orders
     */
    List<OrderDto> getAllOrders();

    /**
     * Get all orders by account ID
     */
    List<OrderDto> getOrdersByAccountId(UUID accountId);

    /**
     * Get unpaid orders where payment method is not COD.
     */
    List<OrderDto> getUnpaidNonCodOrdersByAccountId(UUID accountId);

    /**
     * Get orders by account ID and status
     */
    List<OrderDto> getOrdersByAccountIdAndStatus(UUID accountId, OrderStatus status);

    /**
     * Get all orders that a vendor is responsible for.
     * Responsibility is determined by shipment ownership.
     */
    List<OrderDto> getOrdersByVendorId(UUID vendorId);

    /**
     * Update order
     */
    // OrderDto updateOrder(UUID orderId, UpdateOrderRequest request);

    /**
     * Delete order (Admin only)
     */
    void deleteOrder(UUID orderId);

    // ========== 1.1. SEARCH AND PAGINATION ==========

    /**
     * Search orders with filters and pagination
     */
    Page<OrderDto> searchOrders(SearchOrderRequest searchRequest);

    // ========== 2. STATUS MANAGEMENT ==========

    /**
     * Update order status
     */
    OrderDto updateOrderStatus(UUID orderId, OrderStatus status);

    /**
     * Cancel order
     */
    OrderDto cancelOrder(UUID orderId, CancelOrderRequest request);

    /**
     * Return order
     */
    OrderDto returnOrder(UUID orderId, String reason);

    /**
     * Mark a delivered order as completed by trainee/user.
     */
    OrderDto completeOrderForTrainee(UUID orderId);

    /**
     * Update individual order detail status (for vendors to update item
     * fulfillment).
     * Automatically completes order when all items are COMPLETED.
     * Automatically cancels order when all items are CANCELLED.
     * 
     * @deprecated Use
     *             {@link fpt.edu.sep490.pilahub.service.OrderDetailService#updateOrderDetailStatus}
     *             instead.
     */
    @Deprecated
    void updateOrderDetailStatus(UUID orderDetailId, OrderDetailStatus newStatus);

    /**
     * Check if all items are completed and auto-complete the order
     */
    void checkAndCompleteOrder(UUID orderId);

    /**
     * Process vendor payouts for order details that have reached their payout
     * release date
     * Calculates platform fees and credits vendor wallets
     * Should be called by scheduled jobs or manually by admin
     */
    // void processVendorPayouts(UUID orderId);

    // ========== 3. ORDER ITEMS MANAGEMENT ==========

    /**
     * Add item to existing order (only for PENDING orders)
     */
    OrderDto addItemToOrder(UUID orderId, OrderItemRequest itemRequest);

    /**
     * Remove item from existing order (only for PENDING orders)
     */
    OrderDto removeItemFromOrder(UUID orderId, UUID orderDetailId);

    /**
     * Update item quantity in existing order (only for PENDING orders)
     */
    OrderDto updateOrderItemQuantity(UUID orderId, UUID orderDetailId, Integer newQuantity);

    /**
     * Request refund for order
     */
    // OrderDto requestRefund(UUID orderId, String refundReason);

    /**
     * Process refund for order (Admin/Vendor only)
     */
    OrderDto processRefund(UUID orderId);

    /**
     * Verify payment callback (for payment gateways)
     */
    // OrderDto verifyPaymentCallback(String orderNumber, boolean paymentSuccess,
    // String transactionId);

    /**
     * Generate invoice for order
     */
    // byte[] generateInvoice(UUID orderId);

    /**
     * Update invoice URL for order
     */
    // OrderDto updateInvoiceUrl(UUID orderId, String invoiceUrl);

    /**
     * Get order tracking information
     */
    OrderDto getOrderTracking(String orderNumber);

    /**
     * Pay for order using wallet
     */
    // OrderDto payWithWallet(UUID orderId, UUID accountId);

}

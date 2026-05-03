package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Order information")
public record OrderDto(
                @Schema(description = "Unique order identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID orderId,

                @Schema(description = "Account ID who placed the order", example = "123e4567-e89b-12d3-a456-426614174000") UUID accountId,

                @Schema(description = "Order status", example = "PENDING") OrderStatus status,

                @Schema(description = "Total amount", example = "250000.00") BigDecimal totalAmount,

                @Schema(description = "Discount amount", example = "10000.00") BigDecimal discountAmount,

                @Schema(description = "Shipping fee", example = "30000.00") BigDecimal shippingFee,

                @Schema(description = "Recipient name", example = "John Doe") String recipientName,

                @Schema(description = "Recipient phone", example = "0912345678") String recipientPhone,

                @Schema(description = "Shipping address", example = "123 Main St, Hanoi") String shippingAddress,

                @Schema(description = "Order notes", example = "Please deliver before 5 PM") String notes,

                @Schema(description = "Order number", example = "ORD-20260215-0001") String orderNumber,

                @Schema(description = "Payment method", example = "VNPAY") String paymentMethod,

                @Schema(description = "Whether the order is paid", example = "true") boolean paid,

                @Schema(description = "Whether vendor payout for this order is already released", example = "false") boolean paidOut,

                @Schema(description = "Payment timestamp", example = "2026-02-15T10:30:00Z") Instant paidAt,

                @Schema(description = "Cancelled timestamp", example = "2026-02-15T11:00:00Z") Instant cancelledAt,

                @Schema(description = "Cancellation reason", example = "Customer requested cancellation") String cancellationReason,

                @Schema(description = "All line items in this order") List<OrderDetailDto> orderDetails,

                @Schema(description = "Vendor-scoped shipments; one per vendor in this order") List<ShipmentDto> shipments,

                @Schema(description = "Order creation timestamp", example = "2026-02-15T10:00:00Z") Instant createdAt,

                @Schema(description = "Last update timestamp", example = "2026-02-15T10:00:00Z") Instant updatedAt) {
}

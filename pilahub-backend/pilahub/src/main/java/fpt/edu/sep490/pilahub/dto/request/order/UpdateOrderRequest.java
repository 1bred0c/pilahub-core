package fpt.edu.sep490.pilahub.dto.request.order;

import fpt.edu.sep490.pilahub.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request to update an order")
public record UpdateOrderRequest(
        @Schema(description = "Order status", example = "PENDING") OrderStatus status,

        @Schema(description = "Recipient name", example = "John Doe") @Size(max = 255, message = "Recipient name must not exceed 255 characters") String recipientName,

        @Schema(description = "Recipient phone", example = "0912345678") @Size(max = 20, message = "Recipient phone must not exceed 20 characters") String recipientPhone,

        @Schema(description = "Shipping address", example = "123 Main St, Hanoi") @Size(max = 500, message = "Shipping address must not exceed 500 characters") String shippingAddress,

        @Schema(description = "Shipping fee", example = "30000.00") @DecimalMin(value = "0.0", message = "Shipping fee must not be negative") BigDecimal shippingFee,

        @Schema(description = "Payment method", example = "VNPAY") @Size(max = 50, message = "Payment method must not exceed 50 characters") String paymentMethod,

        @Schema(description = "Order notes", example = "Updated delivery instructions") @Size(max = 1000, message = "Notes must not exceed 1000 characters") String notes) {
}
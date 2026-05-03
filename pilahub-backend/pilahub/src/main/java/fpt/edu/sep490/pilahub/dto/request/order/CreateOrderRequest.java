package fpt.edu.sep490.pilahub.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a new order")
public record CreateOrderRequest(
        @Schema(description = "Recipient name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Recipient name must not be blank") @Size(max = 255, message = "Recipient name must not exceed 255 characters") String recipientName,

        @Schema(description = "Recipient phone", example = "0912345678", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Recipient phone must not be blank") @Size(max = 20, message = "Recipient phone must not exceed 20 characters") String recipientPhone,

        @Schema(description = "Shipping address", example = "123 Main St, Hanoi", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Shipping address must not be blank") @Size(max = 500, message = "Shipping address must not exceed 500 characters") String shippingAddress,

        @Schema(description = "Address ID (optional) - reference to saved address", example = "123e4567-e89b-12d3-a456-426614174000") UUID addressId,
        @Schema(description = "Order items", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Order items must not be null") @NotEmpty(message = "Order must have at least one item") @Valid List<OrderItemRequest> items,

        @Schema(description = "Discount amount", example = "10000.00") @DecimalMin(value = "0.0", message = "Discount amount must not be negative") BigDecimal discountAmount,

        @Schema(description = "Shipping fees grouped by vendor", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Vendor shippings must not be null") @NotEmpty(message = "Vendor shippings must have at least one item") @Valid List<VendorShippingRequest> vendorShippings,

        @Schema(description = "Payment method", example = "WALLET") @Size(max = 50, message = "Payment method must not exceed 50 characters") String paymentMethod,

        @Schema(description = "Order notes", example = "Please deliver before 5 PM") @Size(max = 1000, message = "Notes must not exceed 1000 characters") String notes) {
}
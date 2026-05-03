package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Order detail information — a single product line within an order")
public record OrderDetailDto(
                @Schema(description = "Unique order detail identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID orderDetailId,

                @Schema(description = "Order ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID orderId,

                @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID productId,

                @Schema(description = "Shipment this line item belongs to (null until shipments are created)", example = "123e4567-e89b-12d3-a456-426614174000") UUID shipmentId,

                @Schema(description = "Order detail status", example = "PENDING") OrderDetailStatus status,

                @Schema(description = "Product name", example = "Professional Pilates Mat") String productName,

                @Schema(description = "Product image URL", example = "https://example.com/product.jpg") String productImageUrl,

                @Schema(description = "Quantity", example = "2") Integer quantity,

                @Schema(description = "Unit price at the time of order", example = "59.99") BigDecimal unitPrice,

                @Schema(description = "Subtotal (quantity × unit price − item discount)", example = "119.98") BigDecimal subtotal,

                @Schema(description = "Discount amount for this item", example = "5.00") BigDecimal discountAmount,

                @Schema(description = "Whether installation was requested for this item", example = "true") boolean installationRequest,

                @Schema(description = "Order detail creation timestamp", example = "2026-02-15T10:00:00Z") Instant createdAt,

                @Schema(description = "Last update timestamp", example = "2026-02-15T10:00:00Z") Instant updatedAt) {
}

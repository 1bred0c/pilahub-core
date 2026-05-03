package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Vendor-scoped shipment within an order")
public record ShipmentDto(
        @Schema(description = "Unique shipment identifier") UUID shipmentId,

        @Schema(description = "Order this shipment belongs to") UUID orderId,

        @Schema(description = "Vendor fulfilling this shipment") UUID vendorId,

        @Schema(description = "Vendor business name") String vendorName,

        @Schema(description = "Shipment status", example = "READY_TO_PICK") ShipmentStatus status,

        @Schema(description = "Shipping provider / carrier", example = "GHN") ShippingProvider shippingProvider,

        @Schema(description = "Carrier tracking number", example = "GHN1234567890") String trackingNumber,

        @Schema(description = "Estimated delivery date") Instant estimatedDeliveryAt,

        @Schema(description = "Timestamp when vendor handed package to carrier") Instant shippedAt,

        @Schema(description = "Timestamp when customer received the package") Instant deliveredAt,

        @Schema(description = "Return deadline for items in this shipment") Instant returnDeadline,

        @Schema(description = "Date vendor funds will be released") Instant payoutReleaseDate,

        @Schema(description = "Cancellation timestamp") Instant cancelledAt,

        @Schema(description = "Cancellation reason") String cancellationReason,

        @Schema(description = "Items in this shipment") List<OrderDetailDto> orderDetails,

        @Schema(description = "Creation timestamp") Instant createdAt,

        @Schema(description = "Last update timestamp") Instant updatedAt) {
}

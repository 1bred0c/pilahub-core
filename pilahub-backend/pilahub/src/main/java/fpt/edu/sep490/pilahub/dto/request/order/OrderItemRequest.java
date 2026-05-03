package fpt.edu.sep490.pilahub.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Request to add an item to order")
public record OrderItemRequest(
                @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Product ID must not be null") UUID productId,

                @Schema(description = "Quantity", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Quantity must not be null") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,

                @Schema(description = "Discount amount for this item", example = "5.00") @DecimalMin(value = "0.0", message = "Discount amount must not be negative") BigDecimal discountAmount,

                @Schema(description = "Whether installation is requested for this item", example = "true") Boolean installationRequest) {
}

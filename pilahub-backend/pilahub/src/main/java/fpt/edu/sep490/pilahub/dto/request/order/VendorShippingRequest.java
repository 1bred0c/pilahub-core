package fpt.edu.sep490.pilahub.dto.request.order;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Shipping fee per vendor")
public record VendorShippingRequest(
        @Schema(description = "Vendor ID", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Vendor ID must not be null") UUID vendorId,

        @Schema(description = "Shipping fee for this vendor", example = "20000.00", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Shipping fee must not be null") @DecimalMin(value = "0.0", message = "Shipping fee must not be negative") BigDecimal shippingFee) {
}

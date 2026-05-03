package fpt.edu.sep490.pilahub.dto.request.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@Schema(description = "Request to update the shipping fee of a shipment")
public record UpdateShipmentFeeRequest(

        @Schema(description = "New shipping fee", example = "30000.00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Shipping fee must not be null")
        @DecimalMin(value = "0.0", message = "Shipping fee must not be negative")
        BigDecimal shippingFee
) {
}

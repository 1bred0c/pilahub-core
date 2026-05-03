package fpt.edu.sep490.pilahub.dto.request.shipment;

import fpt.edu.sep490.pilahub.dto.ghn.GhnCreateShipmentOrderClientRequest;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Unified request to create shipment with a selected provider")
public record CreateShipmentRequest(

                @Schema(description = "Shipping provider. Allowed values: GHN or SELF", example = "GHN", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "shippingProvider is required") ShippingProvider shippingProvider,

                @Schema(description = "Payload required when shippingProvider = GHN") @Valid GhnCreateShipmentOrderClientRequest ghnRequest,

                @Schema(description = "Payload required when shippingProvider = SELF") @Valid SelfCreateShipmentRequest selfRequest) {
}
package fpt.edu.sep490.pilahub.dto.request.shipment;

import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update carrier / tracking information for a shipment")
public record UpdateShipmentTrackingRequest(

                @Schema(description = "Shipping provider / carrier", example = "GHN") ShippingProvider shippingProvider,

                @Schema(description = "Carrier tracking number", example = "GHN1234567890", requiredMode = Schema.RequiredMode.REQUIRED) @Size(max = 100, message = "Tracking number must not exceed 100 characters") String trackingNumber) {
}

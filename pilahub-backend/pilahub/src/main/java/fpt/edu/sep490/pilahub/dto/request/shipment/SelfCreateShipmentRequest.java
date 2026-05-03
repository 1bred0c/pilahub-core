package fpt.edu.sep490.pilahub.dto.request.shipment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request details for self-managed shipment creation")
public record SelfCreateShipmentRequest(

        @Schema(description = "Internal or external tracking number for self delivery", example = "SELF-TRACK-0001", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Tracking number must not be blank")
        @Size(max = 100, message = "Tracking number must not exceed 100 characters")
        String trackingNumber,

        @Schema(description = "Optional note for self delivery", example = "Driver will call before arrival")
        @Size(max = 500, message = "Note must not exceed 500 characters")
        String note
) {
}
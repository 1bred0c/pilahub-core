package fpt.edu.sep490.pilahub.dto.ghn;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Minimal client request for creating a GHN shipment order.
 * Backend resolves sender/recipient/package details from the shipment.
 */
@Schema(description = "Client request to create a GHN order from an existing shipment")
public record GhnCreateShipmentOrderClientRequest(

                @Schema(description = "Required note: CHOTHUHANG | CHOXEMHANGKHONGTHU | KHONGCHOXEMHANG", example = "CHOTHUHANG") @NotBlank String requiredNote,

                @Schema(description = "Optional note for the carrier / warehouse", example = "Giao gio hanh chinh") String note) {
}
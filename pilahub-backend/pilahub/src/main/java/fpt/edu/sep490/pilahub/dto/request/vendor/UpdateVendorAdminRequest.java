package fpt.edu.sep490.pilahub.dto.request.vendor;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Admin request to update vendor platform settings")
public record UpdateVendorAdminRequest(
        @Schema(description = "Platform fee percentage charged by the platform", example = "20.0")
        @DecimalMin(value = "0.0", message = "Platform fee percentage must be at least 0")
        @DecimalMax(value = "100.0", message = "Platform fee percentage must not exceed 100")
        Double platformFeePercentage,

        @Schema(description = "Number of days funds are held before release to vendor", example = "3")
        @Min(value = 0, message = "Holding days must be at least 0")
        Integer holdingDays
) {
}
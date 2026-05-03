package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.SubscriptionDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Response for package upgrade with proration details")
public record UpgradePackageResponse(
        @Schema(description = "New subscription details")
        SubscriptionDto subscription,

        @Schema(description = "Refund amount from old package (VND)", example = "15000.00")
        BigDecimal refundAmount,

        @Schema(description = "Number of days remaining from old package", example = "15")
        long daysRemaining,

        @Schema(description = "Original price of new package (VND)", example = "50000.00")
        BigDecimal originalPrice,

        @Schema(description = "Final price after applying refund (VND)", example = "35000.00")
        BigDecimal finalPrice,

        @Schema(description = "Description of the upgrade transaction")
        String description
) {
}

package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.PackageDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Available package for upgrade with discounted price")
public record UpgradeablePackageDto(
        @Schema(description = "Package details")
        PackageDto packageInfo,

        @Schema(description = "Original package price (VND)", example = "100000.00")
        BigDecimal originalPrice,

        @Schema(description = "Proration credit/discount from current package (VND)", example = "15000.00")
        BigDecimal prorationCredit,

        @Schema(description = "Final price after applying discount (VND)", example = "85000.00")
        BigDecimal finalPrice,

        @Schema(description = "Discount percentage", example = "15.0")
        Double discountPercentage,

        @Schema(description = "Description of the discount")
        String discountDescription
) {
}

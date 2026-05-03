package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.PackageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to update an existing package")
public record UpdatePackageRequest(
        @Schema(
                description = "Package name",
                example = "Premium Package"
        )
        @Size(max = 255, message = "Package name must not exceed 255 characters")
        String packageName,

        @Schema(
                description = "Package description",
                example = "Access to all features for 30 days"
        )
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(
                description = "Package price",
                example = "99.99"
        )
        @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
        BigDecimal price,

        @Schema(
                description = "Duration in days",
                example = "30"
        )
        @Min(value = 1, message = "Duration must be at least 1 day")
        Integer durationInDays,

        @Schema(
                description = "Package type",
                example = "MEMBER"
        )
        PackageType packageType,

        @Schema(
                description = "Whether the package is active",
                example = "true"
        )
        Boolean isActive
) {
}

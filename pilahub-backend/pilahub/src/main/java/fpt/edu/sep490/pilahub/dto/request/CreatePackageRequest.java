package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.PackageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "Request to create a new package")
public record CreatePackageRequest(
        @Schema(
                description = "Package name",
                example = "Premium Package",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Package name must not be blank")
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
                example = "99.99",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Price must not be null")
        @DecimalMin(value = "0.00", message = "Price must be greater than or equal to 0")
        BigDecimal price,

        @Schema(
                description = "Duration in days",
                example = "30",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Duration must not be null")
        @Min(value = 1, message = "Duration must be at least 1 day")
        Integer durationInDays,

        @Schema(
                description = "Package type",
                example = "MEMBER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Package type must not be null")
        PackageType packageType,

        @Schema(
                description = "Whether the package is active",
                example = "true"
        )
        Boolean isActive
) {
}

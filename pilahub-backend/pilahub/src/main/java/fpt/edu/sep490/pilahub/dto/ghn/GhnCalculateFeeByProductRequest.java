package fpt.edu.sep490.pilahub.dto.ghn;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to calculate GHN shipping fee from vendor, address, and package details")
public record GhnCalculateFeeByProductRequest(

                @Schema(description = "Service type: 2=Standard, 5=Express", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull Integer serviceTypeId,

                @Schema(description = "Vendor ID whose GHN store is used for origin", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID vendorId,

                @Schema(description = "Address ID of recipient", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull UUID addressId,

                @Schema(description = "Package height (cm)", example = "10", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) Integer height,

                @Schema(description = "Package length (cm)", example = "20", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) Integer length,

                @Schema(description = "Package width (cm)", example = "15", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) Integer width,

                @Schema(description = "Package weight (grams)", example = "500", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) Integer weight,

                @Schema(description = "Quantity of product", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull @Min(1) Integer quantity) {
}

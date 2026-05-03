package fpt.edu.sep490.pilahub.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Filter criteria for product search")
public record ProductFilterRequest(

        @Schema(description = "Search product by name (partial match)")
        String name,

        @Schema(description = "Filter by vendor ID")
        UUID vendorId,

        @Schema(description = "Filter by category ID")
        UUID categoryId,

        @Schema(description = "Filter by brand name")
        String brand,

        @Schema(description = "Minimum price")
        BigDecimal minPrice,

        @Schema(description = "Maximum price")
        BigDecimal maxPrice,

        @Schema(description = "Minimum product rating")
        Double minRating,

        @Schema(description = "Filter active products (mainly for admin)", example = "true")
        Boolean active
) {
}

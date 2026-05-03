package fpt.edu.sep490.pilahub.dto.request.product;

import fpt.edu.sep490.pilahub.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to update a product")
public record UpdateProductRequest(
                @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID categoryId,

                @Schema(description = "Product name", example = "Professional Pilates Mat") @Size(max = 255, message = "Product name must not exceed 255 characters") String name,

                @Schema(description = "Product description", example = "High-quality non-slip Pilates mat") @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

                @Schema(description = "Product image URL", example = "https://example.com/product.jpg") @Size(max = 500, message = "Image URL must not exceed 500 characters") String imageUrl,

                @Schema(description = "Product price", example = "59.99") @DecimalMin(value = "0.0", message = "Price must not be negative") Double price,

                @Schema(description = "Stock quantity", example = "100") @Min(value = 0, message = "Stock quantity must not be negative") Integer stockQuantity,

                @Schema(description = "Product brand", example = "MatPro") @Size(max = 100, message = "Brand must not exceed 100 characters") String brand,

                @Schema(description = "Product specifications", example = "180cm x 60cm, 6mm thickness") @Size(max = 500, message = "Specifications must not exceed 500 characters") String specifications,

                @Schema(description = "Product category type for roadmap grouping", example = "SUPPLEMENT") CategoryType categoryType,

                @Schema(description = "External reference ID for roadmap-linked product", example = "123e4567-e89b-12d3-a456-426614174000") UUID refId,

                @Schema(description = "Product expired date-time (required when resulting categoryType is SUPPLEMENT)", example = "2026-12-31T00:00:00Z") Instant expiredDate,

                @Schema(description = "Package height in cm", example = "15") @Min(value = 1, message = "Height must be at least 1") Integer height,

                @Schema(description = "Package length in cm", example = "15") @Min(value = 1, message = "Length must be at least 1") Integer length,

                @Schema(description = "Package width in cm", example = "15") @Min(value = 1, message = "Width must be at least 1") Integer width,

                @Schema(description = "Package weight in grams", example = "1000") @Min(value = 1, message = "Weight must be at least 1") Integer weight,

                @Schema(description = "Whether installation service is supported", example = "true") Boolean installationSupported,

                @Schema(description = "Regions where this product is supported", example = "[\"Ha Noi\",\"Ho Chi Minh City\"]") List<@Size(max = 255, message = "Each supported region must not exceed 255 characters") String> regionSupported) {
}

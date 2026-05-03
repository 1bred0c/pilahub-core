package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Product information")
public record ProductDto(
        @Schema(description = "Unique product identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID productId,

        @Schema(description = "Vendor ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID vendorId,

        @Schema(description = "Vendor business name", example = "Pilates Pro Shop") String vendorBusinessName,

        @Schema(description = "Category ID", example = "123e4567-e89b-12d3-a456-426614174000") UUID categoryId,

        @Schema(description = "Category name", example = "Pilates Equipment") String categoryName,

        @Schema(description = "Product name", example = "Professional Pilates Mat") String name,

        @Schema(description = "Product description", example = "High-quality non-slip Pilates mat") String description,

        @Schema(description = "Product image URL", example = "https://example.com/product.jpg") String imageUrl,

        @Schema(description = "Product price", example = "59.99") Double price,

        @Schema(description = "Stock quantity", example = "100") Integer stockQuantity,

        @Schema(description = "Product brand", example = "MatPro") String brand,

        @Schema(description = "Product specifications", example = "180cm x 60cm, 6mm thickness") String specifications,

        @Schema(description = "Product category type for roadmap grouping", example = "SUPPLEMENT") CategoryType categoryType,

        @Schema(description = "External reference ID for roadmap-linked product", example = "123e4567-e89b-12d3-a456-426614174000") UUID refId,

        @Schema(description = "Product expired date-time", example = "2026-12-31T00:00:00Z") Instant expiredDate,

        @Schema(description = "Package height in cm", example = "15") Integer height,

        @Schema(description = "Package length in cm", example = "15") Integer length,

        @Schema(description = "Package width in cm", example = "15") Integer width,

        @Schema(description = "Package weight in grams", example = "1000") Integer weight,

        @Schema(description = "Whether installation service is supported for this product", example = "true") boolean installationSupported,

        @Schema(description = "Regions where this product is supported", example = "[\"Ha Noi\",\"Ho Chi Minh City\"]") List<String> regionSupported,

        @Schema(description = "Average rating", example = "4.5") Double avgRating,

        @Schema(description = "Number of reviews", example = "25") Integer reviewCount,

        @Schema(description = "Whether the product is active", example = "true") boolean active,

        @Schema(description = "Whether this product is marked as a rule violation", example = "false") boolean ruleViolation,

        @Schema(description = "Product creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z") Instant updatedAt) {
}

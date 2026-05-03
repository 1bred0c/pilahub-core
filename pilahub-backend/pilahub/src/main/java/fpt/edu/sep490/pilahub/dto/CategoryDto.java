package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Category information")
public record CategoryDto(
                @Schema(description = "Unique category identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID categoryId,

                @Schema(description = "Parent category ID (for subcategories)", example = "123e4567-e89b-12d3-a456-426614174000") UUID parentCategoryId,

                @Schema(description = "Category name", example = "Pilates Equipment") String name,

                @Schema(description = "Category description", example = "All equipment for Pilates workouts") String description,

                @Schema(description = "Category image URL", example = "https://example.com/category.jpg") String imageUrl,

                @Schema(description = "Category type (SUPPLEMENT or EQUIPMENT)", example = "SUPPLEMENT") CategoryType categoryType,

                @Schema(description = "Whether the category is active", example = "true") boolean active,

                @Schema(description = "Category creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt,

                @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z") Instant updatedAt) {
}

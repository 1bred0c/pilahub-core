package fpt.edu.sep490.pilahub.dto.request.category;

import fpt.edu.sep490.pilahub.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Request to update a category")
public record UpdateCategoryRequest(
        @Schema(description = "Parent category ID (for subcategories)", example = "123e4567-e89b-12d3-a456-426614174000") UUID parentCategoryId,

        @Schema(description = "Category name", example = "Pilates Equipment") @Size(max = 255, message = "Category name must not exceed 255 characters") String name,

        @Schema(description = "Category description", example = "All equipment for Pilates workouts") @Size(max = 2000, message = "Description must not exceed 2000 characters") String description,

        @Schema(description = "Category image URL", example = "https://example.com/category.jpg") @Size(max = 500, message = "Image URL must not exceed 500 characters") String imageUrl,

        @Schema(description = "Category type (SUPPLEMENT or EQUIPMENT)", example = "SUPPLEMENT") CategoryType categoryType) {
}

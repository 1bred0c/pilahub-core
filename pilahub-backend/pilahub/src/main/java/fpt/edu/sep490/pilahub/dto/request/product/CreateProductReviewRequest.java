package fpt.edu.sep490.pilahub.dto.request.product;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.util.UUID;

@Schema(description = "Request to create a product review")
public record CreateProductReviewRequest(
        @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Product ID must not be null")
        UUID productId,

        @Schema(description = "Rating (1-5)", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Rating must not be null")
        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @Schema(description = "Review comment", example = "Excellent quality mat!")
        @Size(max = 2000, message = "Comment must not exceed 2000 characters")
        String comment,

        @Schema(description = "Review image URL", example = "https://example.com/review.jpg")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl
) {
}

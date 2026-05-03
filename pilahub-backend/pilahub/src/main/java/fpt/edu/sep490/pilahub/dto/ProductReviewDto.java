package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Product review information")
public record ProductReviewDto(
        @Schema(description = "Unique review identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID reviewId,

        @Schema(description = "Product ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID productId,

        @Schema(description = "Product name", example = "Professional Pilates Mat")
        String productName,

        @Schema(description = "Account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Reviewer name", example = "John Doe")
        String reviewerName,

        @Schema(description = "Rating (1-5)", example = "5")
        Integer rating,

        @Schema(description = "Review comment", example = "Excellent quality mat!")
        String comment,

        @Schema(description = "Review image URL", example = "https://example.com/review.jpg")
        String imageUrl,

        @Schema(description = "Whether this is a verified purchase", example = "true")
        boolean verifiedPurchase,

        @Schema(description = "Review creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

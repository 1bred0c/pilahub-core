package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Post reaction information")
public record PostReactionDto(
        @Schema(description = "Post ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postId,

        @Schema(description = "Reacting account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Reacting account display name", example = "Le Thi C")
        String accountName,

        @Schema(description = "Reaction creation time", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}


package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Reaction summary for a post")
public record PostReactionSummaryDto(
        @Schema(description = "Post ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postId,

        @Schema(description = "Total reaction count", example = "25")
        long reactionCount,

        @Schema(description = "Whether current user has reacted", example = "true")
        boolean reactedByMe
) {
}


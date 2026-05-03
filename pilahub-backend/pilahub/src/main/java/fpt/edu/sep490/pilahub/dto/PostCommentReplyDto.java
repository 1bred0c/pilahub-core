package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Reply comment information")
public record PostCommentReplyDto(
        @Schema(description = "Comment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID commentId,

        @Schema(description = "Post ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postId,

        @Schema(description = "Author account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Author display name", example = "Nguyen Van B")
        String accountName,

        @Schema(description = "Reply content", example = "Thanks for sharing this tip!")
        String content,

        @Schema(description = "Parent comment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID parentCommentId,

        @Schema(description = "Creation time", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}


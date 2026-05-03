package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Post comment information")
public record PostCommentDto(
        @Schema(description = "Comment ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID commentId,

        @Schema(description = "Post ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postId,

        @Schema(description = "Author account ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID accountId,

        @Schema(description = "Author display name", example = "Nguyen Van A")
        String accountName,

        @Schema(description = "Comment content", example = "Great post, coach!")
        String content,

        @Schema(description = "Parent comment ID, null for top-level", nullable = true)
        UUID parentCommentId,

        @Schema(description = "Creation time", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Preview replies (max 3)")
        List<PostCommentReplyDto> replies,

        @Schema(description = "True when this root comment has more than preview replies", example = "false")
        boolean hasMoreReplies
) {
}


package fpt.edu.sep490.pilahub.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a post comment")
public record CreatePostCommentRequest(
        @Schema(description = "Comment content", example = "This is very useful, thanks!", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Content must not be blank")
        @Size(max = 2000, message = "Content must not exceed 2000 characters")
        String content,

        @Schema(description = "Parent comment ID for reply; null for top-level comment", nullable = true)
        UUID parentCommentId
) {
}


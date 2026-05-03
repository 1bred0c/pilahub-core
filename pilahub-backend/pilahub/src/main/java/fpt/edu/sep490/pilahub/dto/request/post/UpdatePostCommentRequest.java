package fpt.edu.sep490.pilahub.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a post comment")
public record UpdatePostCommentRequest(
        @Schema(description = "Updated comment content", example = "Updated comment text", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Content must not be blank")
        @Size(max = 2000, message = "Content must not exceed 2000 characters")
        String content
) {
}


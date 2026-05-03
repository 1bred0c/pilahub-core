package fpt.edu.sep490.pilahub.dto.request.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Request to create a post")
public record CreatePostRequest(
        @Schema(description = "Post content", example = "Core activation basics for beginners", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Content must not be blank")
        @Size(max = 5000, message = "Content must not exceed 5000 characters")
        String content,

        @Schema(description = "Ordered media list attached to the post")
        List<@Valid PostMediaUpsertRequest> medias
) {
}


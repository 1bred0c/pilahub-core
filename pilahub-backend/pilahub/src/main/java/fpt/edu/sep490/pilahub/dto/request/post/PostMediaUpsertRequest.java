package fpt.edu.sep490.pilahub.dto.request.post;

import fpt.edu.sep490.pilahub.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Media item in a post")
public record PostMediaUpsertRequest(
        @Schema(description = "Media type", example = "IMAGE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Media type must not be null")
        MediaType mediaType,

        @Schema(description = "Media URL", example = "https://example.com/post-media.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Media URL must not be null")
        @Size(max = 500, message = "Media URL must not exceed 500 characters")
        String mediaUrl,

        @Schema(description = "Display order in post", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Sort order must not be null")
        @Min(value = 1, message = "Sort order must be at least 1")
        Integer sortOrder
) {
}


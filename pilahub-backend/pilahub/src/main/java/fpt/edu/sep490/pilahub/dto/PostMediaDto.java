package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Post media information")
public record PostMediaDto(
        @Schema(description = "Unique post media identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postMediaId,

        @Schema(description = "Media type", example = "IMAGE")
        MediaType mediaType,

        @Schema(description = "Media URL", example = "https://example.com/post-media.jpg")
        String mediaUrl,

        @Schema(description = "Display order in the post", example = "1")
        Integer sortOrder
) {
}


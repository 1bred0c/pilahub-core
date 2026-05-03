package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Post information")
public record PostDto(
        @Schema(description = "Unique post identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID postId,

        @Schema(description = "Coach ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID coachId,

        @Schema(description = "Coach name", example = "Nguyen Van A")
        String coachName,

        @Schema(description = "Post content", example = "Today I want to share a simple core-strength routine...")
        String content,

        @Schema(description = "Post creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Ordered media list attached to the post")
        List<PostMediaDto> medias
) {
}


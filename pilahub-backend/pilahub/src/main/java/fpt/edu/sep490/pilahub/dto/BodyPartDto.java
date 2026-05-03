package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Body part information")
public record BodyPartDto(
        @Schema(description = "Unique body part identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID bodyPartId,

        @Schema(description = "Body part name", example = "Chest")
        String name,

        @Schema(description = "Body part description", example = "The chest muscles including pectoralis major and minor")
        String description,

        @Schema(description = "Body part creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

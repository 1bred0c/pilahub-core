package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "System configuration")
public record SystemConfigDto(
        @Schema(description = "Unique system config identifier", example = "123e4567-e89b-12d3-a456-426614174000") UUID configId,

        @Schema(description = "Configuration key", example = "PLATFORM_FEE_PERCENTAGE") String key,

        @Schema(description = "Configuration value", example = "20.0") String value,

        @Schema(description = "Configuration description", example = "Default platform fee percentage") String description,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z") Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z") Instant updatedAt) {
}

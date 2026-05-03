package fpt.edu.sep490.pilahub.dto.request.systemconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a system config")
public record UpdateSystemConfigRequest(
        @Schema(description = "Configuration key", example = "PLATFORM_FEE_PERCENTAGE") @Size(min = 1, max = 100, message = "Config key must be between 1 and 100 characters") String key,

        @Schema(description = "Configuration value", example = "20.0") @Size(min = 1, max = 500, message = "Config value must be between 1 and 500 characters") String value,

        @Schema(description = "Configuration description", example = "Default platform fee percentage") @Size(max = 1000, message = "Description must not exceed 1000 characters") String description) {
}

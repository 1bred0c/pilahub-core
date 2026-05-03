package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to subscribe to a package")
public record SubscribePackageRequest(
        @Schema(
                description = "Package ID to subscribe",
                example = "123e4567-e89b-12d3-a456-426614174000",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Package ID must not be null")
        UUID packageId
) {
}

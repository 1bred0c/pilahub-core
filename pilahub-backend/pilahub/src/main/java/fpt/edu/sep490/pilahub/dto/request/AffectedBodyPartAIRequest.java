package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Affected body part information")
public record AffectedBodyPartAIRequest(
        @Schema(description = "Body part name", example = "Đầu gối")
        String name,

        @Schema(description = "Body part description")
        String description
) {
}

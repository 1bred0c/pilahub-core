package pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Affected body part information for AI request")
public record AffectedBodyPartAIRequest(
        @Schema(description = "Body part name", example = "Lower Back")
        String name,

        @Schema(description = "Body part description", example = "Lumbar region of the spine")
        String description
) {
}

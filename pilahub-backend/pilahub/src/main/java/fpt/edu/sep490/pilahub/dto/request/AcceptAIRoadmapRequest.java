package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.dto.response.RoadmapAIResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

@Schema(description = "Request to accept and save AI-generated roadmap (for trainees and coaches)")
public record AcceptAIRoadmapRequest(
        @Schema(description = "AI-generated roadmap response to save", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "AI response must not be null")
        @Valid
        RoadmapAIResponse aiResponse,

        @Schema(description = "Trainee ID (optional - defaults to current user if trainee, required for coaches)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Primary fitness goal ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Primary goal must not be null")
        UUID primaryGoalId,

        @Schema(description = "Secondary fitness goal IDs (optional)", example = "[\"uuid1\", \"uuid2\"]")
        @Size(max = 4, message = "Maximum 4 secondary goals allowed")
        List<UUID> secondaryGoalIds
) {
}

package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Request to create a new roadmap")
public record CreateRoadmapRequest(
        @Schema(description = "Roadmap title", example = "Full Stack Developer Path", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Title must not be blank")
        @Size(max = 255, message = "Title must not exceed 255 characters")
        String title,

        @Schema(description = "Roadmap description", example = "Complete roadmap to become a full stack developer")
        @Size(max = 1000, message = "Description must not exceed 1000 characters")
        String description,

        @Schema(description = "Roadmap start date", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "Roadmap end date", example = "2026-12-31T23:59:59Z")
        Instant endDate,

        @Schema(description = "Source of the roadmap", example = "roadmap.sh")
        @Size(max = 255, message = "Source must not exceed 255 characters")
        String source,

        @Schema(description = "Trainee ID (optional - defaults to current user if trainee, required for coaches/admins)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Primary fitness goal ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Primary goal must not be null")
        UUID primaryGoalId,

        @Schema(description = "Secondary fitness goal IDs (optional)", example = "[\"uuid1\", \"uuid2\"]")
        @Size(max = 4, message = "Maximum 4 secondary goals allowed")
        List<UUID> secondaryGoalIds
) {
}

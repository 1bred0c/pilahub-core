package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Roadmap information")
public record RoadmapDto(
        @Schema(description = "Unique roadmap identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID roadmapId,

        @Schema(description = "Roadmap title", example = "Full Stack Developer Path")
        String title,

        @Schema(description = "Roadmap description", example = "Complete roadmap to become a full stack developer")
        String description,

        @Schema(description = "Roadmap start date", example = "2026-01-01T00:00:00Z")
        Instant startDate,

        @Schema(description = "Roadmap end date", example = "2026-12-31T23:59:59Z")
        Instant endDate,

        @Schema(description = "Progress percentage", example = "45")
        Integer progressPercent,

        @Schema(description = "Source of the roadmap", example = "roadmap.sh")
        String source,

        @Schema(description = "Whether the roadmap is active", example = "true")
        String status,

        @Schema(description = "List of fitness goals for this roadmap")
        List<RoadmapGoalDto> goals,

        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Coach ID (optional)", example = "123e4567-e89b-12d3-a456-426614174001")
        UUID coachId,

        @Schema(description = "Initial health profile ID snapshot when roadmap is created", example = "123e4567-e89b-12d3-a456-426614174002")
        UUID initialHealthProfileId,

        @Schema(description = "Final health profile ID snapshot after roadmap reaches 100% progress", example = "123e4567-e89b-12d3-a456-426614174003")
        UUID finalHealthProfileId,

        @Schema(description = "Total amount for this roadmap if assigned by coach (pricePerHour * hoursPerSlot * totalSchedules)", example = "1200000.00")
        BigDecimal totalAmount,

        @Schema(description = "Roadmap creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

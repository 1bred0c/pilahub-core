package fpt.edu.sep490.pilahub.dto.request.roadmap;

import fpt.edu.sep490.pilahub.enums.RoadmapStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "Filter criteria for roadmap search")
public record RoadmapFilterRequest(
        @Schema(description = "Filter by roadmap title (partial match)", example = "Weight Loss")
        String title,

        @Schema(description = "Filter by roadmap status", example = "IN_PROGRESS")
        RoadmapStatus status,

        @Schema(description = "Filter by roadmap source", example = "AI")
        String source,

        @Schema(description = "Filter roadmaps starting from this date", example = "2026-01-01T00:00:00Z")
        Instant startDateFrom,

        @Schema(description = "Filter roadmaps starting until this date", example = "2026-12-31T23:59:59Z")
        Instant startDateTo,

        @Schema(description = "Filter roadmaps ending from this date", example = "2026-01-01T00:00:00Z")
        Instant endDateFrom,

        @Schema(description = "Filter roadmaps ending until this date", example = "2026-12-31T23:59:59Z")
        Instant endDateTo
) {
}

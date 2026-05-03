package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Health profile metrics response for chart visualization")
public record HealthProfileMetricsResponse(
        @Schema(description = "Trainee ID", example = "9e7b8c1a-45c2-4e90-8c2b-5e0cfa0e3a91")
        UUID traineeId,

        @Schema(description = "Latest profile ID", example = "2a4c2d18-1c39-4d22-8e7b-b0e0e4a9e1c1")
        UUID latestProfileId,

        @Schema(description = "Health metrics data points over time")
        HealthMetrics metrics,

        @Schema(description = "Comparison between latest and previous profile")
        MetricComparison latestComparison
) {
}


package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Roadmap goal information")
public record RoadmapGoalDto(
        @Schema(description = "Unique roadmap goal identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID roadmapGoalId,

        @Schema(description = "Fitness goal ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID goalId,

        @Schema(description = "Fitness goal code", example = "MUSCLE_GAIN")
        String code,

        @Schema(description = "Vietnamese name", example = "Tăng cơ")
        String vietnameseName,

        @Schema(description = "Whether this is the primary goal", example = "true")
        Boolean isPrimary,

        @Schema(description = "Display order", example = "1")
        Integer goalOrder
) {
}

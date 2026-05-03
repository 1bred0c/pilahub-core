package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Assessment form submitted for a live session")
public record SessionAssessmentDto(
        @Schema(description = "Live session ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID liveSessionId,

        @Schema(description = "Coach ID", example = "223e4567-e89b-12d3-a456-426614174000")
        UUID coachId,

        @Schema(description = "Trainee ID", example = "323e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Submitted timestamp", example = "2026-04-22T10:30:00Z")
        Instant submittedAt,

        @Schema(description = "Scores for each criterion")
        List<AssessmentResultDto> results
) {
}


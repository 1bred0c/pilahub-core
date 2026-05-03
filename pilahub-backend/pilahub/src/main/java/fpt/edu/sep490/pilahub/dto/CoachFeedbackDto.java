package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Coach feedback information")
public record CoachFeedbackDto(
        @Schema(description = "Unique feedback identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID feedbackId,

        @Schema(description = "Coach ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID coachId,

        @Schema(description = "Coach's full name", example = "Jane Smith")
        String coachFullName,

        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Trainee's full name", example = "John Doe")
        String traineeFullName,

        @Schema(description = "Trainee's avatar URL", example = "https://example.com/avatar.jpg")
        String traineeAvatarUrl,

        @Schema(description = "Rating (1-5)", example = "5")
        Integer rating,

        @Schema(description = "Feedback comment", example = "Excellent coach! Very professional and helpful.")
        String comment,

        @Schema(description = "Feedback creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Feedback update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

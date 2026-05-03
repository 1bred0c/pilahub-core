package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Trainee information")
public record TraineeDto(
        @Schema(description = "Unique trainee identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Trainee's full name", example = "John Doe")
        String fullName,

        @Schema(description = "Trainee's age", example = "25")
        Integer age,

        @Schema(description = "Trainee's gender", example = "MALE")
        Gender gender,

        @Schema(description = "Trainee's avatar URL", example = "https://example.com/avatar.jpg")
        String avatarUrl,

        @Schema(description = "Trainee's workout level", example = "BEGINNER")
        WorkoutLevel workoutLevel,

        @Schema(description = "Trainee's workout frequency", example = "THREE_TIMES_PER_WEEK")
        WorkoutFrequency workoutFrequency,

        @Schema(description = "Account creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}

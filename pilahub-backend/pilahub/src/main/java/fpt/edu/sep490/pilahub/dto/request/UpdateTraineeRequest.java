package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to update trainee information")
public record UpdateTraineeRequest(
        @Schema(
                description = "Trainee's full name",
                example = "John Doe"
        )
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Schema(
                description = "Trainee's age",
                example = "25"
        )
        @Min(value = 1, message = "Age must be at least 1")
        @Max(value = 150, message = "Age must not exceed 150")
        Integer age,

        @Schema(
                description = "Trainee's gender",
                example = "MALE"
        )
        Gender gender,

        @Schema(
                description = "Trainee's avatar URL",
                example = "https://example.com/avatar.jpg"
        )
        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        String avatarUrl,

        @Schema(
                description = "Trainee's workout level",
                example = "BEGINNER"
        )
        WorkoutLevel workoutLevel,

        @Schema(
                description = "Trainee's workout frequency",
                example = "THREE_TIMES_PER_WEEK"
        )
        WorkoutFrequency workoutFrequency
) {
}

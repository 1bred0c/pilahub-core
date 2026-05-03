package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.Gender;
import fpt.edu.sep490.pilahub.enums.WorkoutFrequency;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to create a new trainee")
public record CreateTraineeRequest(
        @Schema(
                description = "Trainee's full name",
                example = "John Doe",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Full name must not be blank")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Schema(
                description = "Trainee's age",
                example = "25",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Age must not be null")
        @Min(value = 1, message = "Age must be at least 1")
        @Max(value = 150, message = "Age must not exceed 150")
        Integer age,

        @Schema(
                description = "Trainee's gender",
                example = "MALE",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Gender must not be null")
        Gender gender,

        @Schema(
                description = "Trainee's avatar URL",
                example = "https://example.com/avatar.jpg"
        )
        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        String avatarUrl,

        @Schema(
                description = "Trainee's workout level",
                example = "BEGINNER",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Workout level must not be null")
        WorkoutLevel workoutLevel,

        @Schema(
                description = "Trainee's workout frequency",
                example = "THREE_TIMES_PER_WEEK",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "Workout frequency must not be null")
        WorkoutFrequency workoutFrequency
) {
}

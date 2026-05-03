package fpt.edu.sep490.pilahub.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request to enroll a trainee in a course")
public record CreateTraineeCourseRequest(
        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Trainee ID must not be null")
        UUID traineeId,

        @Schema(description = "Course ID to enroll in", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Course ID must not be null")
        UUID courseId
) {
}

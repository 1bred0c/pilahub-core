package fpt.edu.sep490.pilahub.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@Schema(description = "Request to create a new course lesson relationship")
public record CreateCourseLessonRequest(
        @Schema(description = "Lesson ID", example = "123e4567-e89b-12d3-a456-426614174000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Lesson ID must not be null")
        UUID lessonId,

        @Schema(description = "Display order", example = "1")
        @Min(value = 1, message = "Order must be at least 1")
        Integer displayOrder,

        @Schema(description = "Additional notes", example = "Complete before moving to next lesson")
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}

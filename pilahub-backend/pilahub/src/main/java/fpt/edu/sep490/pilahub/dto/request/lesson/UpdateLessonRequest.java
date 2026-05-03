package fpt.edu.sep490.pilahub.dto.request.lesson;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a lesson")
public record UpdateLessonRequest(
        @Schema(description = "Lesson name", example = "Morning Stretching Routine")
        @Size(max = 255, message = "Lesson name must not exceed 255 characters")
        String name,

        @Schema(description = "Lesson description", example = "A comprehensive morning stretching session")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description
) {
}

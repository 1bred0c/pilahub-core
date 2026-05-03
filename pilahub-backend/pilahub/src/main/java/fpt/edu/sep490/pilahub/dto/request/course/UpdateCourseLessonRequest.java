package fpt.edu.sep490.pilahub.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to update a course lesson relationship")
public record UpdateCourseLessonRequest(
        @Schema(description = "Display order", example = "1")
        @Min(value = 1, message = "Order must be at least 1")
        Integer displayOrder,

        @Schema(description = "Additional notes", example = "Complete before moving to next lesson")
        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}

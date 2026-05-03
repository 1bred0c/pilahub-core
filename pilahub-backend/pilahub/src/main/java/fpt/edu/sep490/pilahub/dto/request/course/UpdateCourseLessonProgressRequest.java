package fpt.edu.sep490.pilahub.dto.request.course;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request to update a course lesson progress")
public record UpdateCourseLessonProgressRequest(
        @Schema(description = "Whether the lesson is completed", example = "true")
        Boolean completed
) {
}

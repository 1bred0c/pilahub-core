package fpt.edu.sep490.pilahub.dto.request.course;

import fpt.edu.sep490.pilahub.enums.DifficultyLevel;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to create a new course")
public record CreateCourseRequest(
        @Schema(description = "Course name", example = "Beginner Pilates Program", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Course name must not be blank")
        @Size(max = 255, message = "Course name must not exceed 255 characters")
        String name,

        @Schema(description = "Course description", example = "A comprehensive 8-week pilates course")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @Schema(description = "Course image URL", example = "https://example.com/images/course.jpg")
        @Size(max = 500, message = "Image URL must not exceed 500 characters")
        String imageUrl,

        @Schema(description = "Difficulty level", example = "BEGINNER")
        DifficultyLevel level,

        @Schema(description = "Course price", example = "99.99")
        @Min(value = 0, message = "Price must be at least 0")
        Double price
) {
}

package fpt.edu.sep490.pilahub.dto.request.coach;

import fpt.edu.sep490.pilahub.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to create a new coach profile")
public record CreateCoachRequest(
        @Schema(description = "Coach's full name", example = "Jane Smith", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Full name must not be blank")
        @Size(max = 255, message = "Full name must not exceed 255 characters")
        String fullName,

        @Schema(description = "Coach's age", example = "35", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Age must not be null")
        @Min(value = 18, message = "Age must be at least 18")
        @Max(value = 150, message = "Age must not exceed 150")
        Integer age,

        @Schema(description = "Coach's gender", example = "FEMALE", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Gender must not be null")
        Gender gender,

        @Schema(description = "Coach's avatar URL", example = "https://example.com/coach-avatar.jpg")
        @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
        String avatarUrl,

        @Schema(description = "Coach's biography", example = "Certified Pilates instructor with extensive experience")
        @Size(max = 2000, message = "Bio must not exceed 2000 characters")
        String bio,

        @Schema(description = "Years of experience", example = "10")
        @Min(value = 0, message = "Years of experience must not be negative")
        Integer yearsOfExperience,

        @Schema(description = "Coach's specialization", example = "Pilates Rehabilitation, Core Strengthening")
        @Size(max = 500, message = "Specialization must not exceed 500 characters")
        String specialization,

        @Schema(description = "URL to coach's certifications", example = "https://example.com/certifications.pdf")
        @Size(max = 500, message = "Certifications URL must not exceed 500 characters")
        String certificationsUrl
) {
}

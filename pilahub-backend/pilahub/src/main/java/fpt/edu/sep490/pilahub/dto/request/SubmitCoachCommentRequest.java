package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request to submit coach comment for a completed live session")
public record SubmitCoachCommentRequest(
        @Schema(
                description = "Coach's feedback and advice for trainee",
                example = "Great session! Focus on maintaining proper form during squats. Try to increase reps gradually."
        )
        @NotBlank(message = "Comment must not be blank")
        @Size(max = 2000, message = "Comment must not exceed 2000 characters")
        String comment
) {
}


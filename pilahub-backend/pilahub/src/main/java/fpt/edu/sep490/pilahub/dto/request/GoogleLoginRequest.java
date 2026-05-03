package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Google login request - only requires ID token, all other info can be updated later")
public record GoogleLoginRequest(
        @Schema(
                description = "Google ID token for verification (email will be extracted from this token)",
                example = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1...",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Google ID token must not be blank")
        String googleIdToken
) {
}




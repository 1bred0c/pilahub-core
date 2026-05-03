package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Google authentication response")
public record GoogleAuthResponse(
        @Schema(
                description = "Indicates if user needs to complete registration",
                example = "false"
        )
        boolean requiresRegistration,

        @Schema(
                description = "Message for the user",
                example = "Login successful"
        )
        String message,

        @Schema(
                description = "Authentication details (null if requiresRegistration is true)"
        )
        AuthResponse authResponse
) {
    // Factory method for successful login
    public static GoogleAuthResponse success(AuthResponse authResponse) {
        return new GoogleAuthResponse(false, "Login successful", authResponse);
    }

    // Factory method for registration required
    public static GoogleAuthResponse requiresRegistration(String email) {
        return new GoogleAuthResponse(
                true,
                "Please provide phone number and password to complete registration",
                null
        );
    }
}


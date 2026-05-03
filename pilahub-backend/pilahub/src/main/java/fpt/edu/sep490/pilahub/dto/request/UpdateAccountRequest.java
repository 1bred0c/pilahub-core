package fpt.edu.sep490.pilahub.dto.request;

import fpt.edu.sep490.pilahub.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Request to update account information")
public record UpdateAccountRequest(
        @Schema(
                description = "User's email address",
                example = "user@example.com"
        )
        @Email(message = "Email format is invalid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Schema(
                description = "User's phone number",
                example = "+1234567890"
        )
        @Pattern(
                regexp = "^\\+?[0-9]{9,15}$",
                message = "Phone number format is invalid"
        )
        String phoneNumber,

        @Schema(
                description = "User's role in the system",
                example = "TRAINEE"
        )
        Role role,

        @Schema(
                description = "Whether the account is active",
                example = "true"
        )
        Boolean active,

        @Schema(
                description = "Whether the email has been verified",
                example = "true"
        )
        Boolean emailVerified
) {
}

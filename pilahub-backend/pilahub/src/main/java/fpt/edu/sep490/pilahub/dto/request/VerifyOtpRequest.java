package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Email verification request with OTP code")
public record VerifyOtpRequest(
        @Schema(
                description = "User's email address",
                example = "user@example.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @Schema(
                description = "6-digit OTP code sent to email",
                example = "123456",
                requiredMode = Schema.RequiredMode.REQUIRED,
                minLength = 6,
                maxLength = 6
        )
        @NotBlank(message = "OTP code must not be blank")
        @Size(min = 6, max = 6, message = "OTP code must be 6 digits")
        @Pattern(regexp = "^[0-9]{6}$", message = "OTP code must contain only digits")
        String otpCode
) {
}

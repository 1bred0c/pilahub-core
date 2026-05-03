package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request containing user credentials")
public record LoginRequest(
        @Schema(
                description = "User's email address",
                example = "admin@pila.com",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Email must not be blank")
        @Email(message = "Email format is invalid")
        String email,

        @Schema(
                description = "User's password",
                example = "tuongdeptrai123",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Password must not be blank")
        String password
) {
}

package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Authentication response containing JWT token and user details")
public record AuthResponse(
        @Schema(
                description = "JWT access token for authentication",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String accessToken,

        @Schema(
                description = "JWT refresh token for renewing access token",
                example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        )
        String refreshToken,

        @Schema(
                description = "Token type (always Bearer)",
                example = "Bearer"
        )
        String tokenType,

        @Schema(
                description = "Access token expiration time in milliseconds",
                example = "86400000"
        )
        Long expiresIn,

        @Schema(
                description = "Refresh token expiration time in milliseconds",
                example = "604800000"
        )
        Long refreshExpiresIn,

        @Schema(description = "User account information")
        AccountDto account
) {
    public AuthResponse(String accessToken, String refreshToken, Long expiresIn, Long refreshExpiresIn, AccountDto account) {
        this(accessToken, refreshToken, "Bearer", expiresIn, refreshExpiresIn, account);
    }
}

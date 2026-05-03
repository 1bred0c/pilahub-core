package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Live session tokens for Agora")
public record LiveSessionTokenDto(
        @Schema(description = "Agora channel name", example = "session_123e4567")
        String channelName,

        @Schema(description = "User UID for Agora", example = "10001")
        Integer uid,

        @Schema(description = "Access token for Agora", example = "006abc...")
        String token,

        @Schema(description = "Token expiration in seconds", example = "3600")
        Integer expirationSeconds
) {
}


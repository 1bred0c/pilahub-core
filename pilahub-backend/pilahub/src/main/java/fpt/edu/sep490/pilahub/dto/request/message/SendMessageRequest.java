package fpt.edu.sep490.pilahub.dto.request.message;

import fpt.edu.sep490.pilahub.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "Request payload for sending a private message")
public record SendMessageRequest(
        @NotNull(message = "Receiver id is required")
        @Schema(description = "Receiver account identifier")
        UUID receiverId,

        @NotBlank(message = "Content must not be blank")
        @Schema(description = "Plain text for TEXT, direct URL for IMAGE/VIDEO/LINK")
        String content,

        @NotNull(message = "Message type is required")
        @Schema(description = "Message type", example = "TEXT")
        MessageType messageType
) {
}


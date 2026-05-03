package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Compact message payload used for conversation preview")
public record MessagePreviewDto(
        @Schema(description = "Message identifier")
        UUID messageId,

        @Schema(description = "Sender account identifier")
        UUID senderId,

        @Schema(description = "Receiver account identifier")
        UUID receiverId,

        @Schema(description = "Message body text or URL")
        String content,

        @Schema(description = "Message content type")
        MessageType messageType,

        @Schema(description = "Read status")
        boolean read,

        @Schema(description = "Soft-delete status")
        boolean deleted,

        @Schema(description = "Creation time")
        Instant createAt
) {
}


package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.ConversationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Conversation details")
public record ConversationDetailDto(
        @Schema(description = "Conversation identifier")
        UUID conversationId,

        @Schema(description = "First participant account identifier")
        UUID account1Id,

        @Schema(description = "Second participant account identifier")
        UUID account2Id,

        @Schema(description = "Conversation type based on participant roles")
        ConversationType conversationType,

        @Schema(description = "Most recent message preview")
        MessagePreviewDto lastMessage,

        @Schema(description = "Timestamp of the most recent message")
        Instant lastMessageAt
) {
}


package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.ConversationType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Inbox item for a private conversation")
public record ConversationInboxDto(
        @Schema(description = "Conversation identifier")
        UUID conversationId,

        @Schema(description = "Other participant account identifier")
        UUID otherUserId,

        @Schema(description = "Conversation type based on participant roles")
        ConversationType conversationType,

        @Schema(description = "Most recent message preview")
        MessagePreviewDto lastMessage,

        @Schema(description = "Timestamp of the most recent message")
        Instant lastMessageAt,

        @Schema(description = "Unread message count for current user")
        long unreadCount
) {
}


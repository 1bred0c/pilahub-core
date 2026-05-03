package fpt.edu.sep490.pilahub.repository;

import java.util.UUID;

public interface ConversationUnreadCountProjection {
    UUID getConversationId();

    long getUnreadCount();
}


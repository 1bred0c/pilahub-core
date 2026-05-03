package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.ConversationDetailDto;
import fpt.edu.sep490.pilahub.dto.ConversationInboxDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ConversationService {

    Page<ConversationInboxDto> getInbox(UUID currentUserId, Pageable pageable);

    long getTotalUnreadCount(UUID currentUserId);

    Optional<ConversationDetailDto> getConversationByUser(UUID currentUserId, UUID receiverId);

    void markConversationAsRead(UUID conversationId, UUID currentUserId);
}


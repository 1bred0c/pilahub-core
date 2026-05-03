package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.MessageDto;
import fpt.edu.sep490.pilahub.dto.request.message.SendMessageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MessageService {

    Page<MessageDto> getConversationMessages(UUID conversationId, UUID currentUserId, Pageable pageable);

    MessageDto sendMessage(UUID senderId, SendMessageRequest request);

    void deleteMessage(UUID messageId, UUID currentUserId);
}


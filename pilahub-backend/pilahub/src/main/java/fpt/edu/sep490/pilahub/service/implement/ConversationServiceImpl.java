package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.ConversationDetailDto;
import fpt.edu.sep490.pilahub.dto.ConversationInboxDto;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.ConversationMapper;
import fpt.edu.sep490.pilahub.mapper.MessageMapper;
import fpt.edu.sep490.pilahub.pojo.Conversation;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.ConversationRepository;
import fpt.edu.sep490.pilahub.repository.ConversationUnreadCountProjection;
import fpt.edu.sep490.pilahub.repository.MessagesRepository;
import fpt.edu.sep490.pilahub.service.ConversationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConversationServiceImpl implements ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessagesRepository messagesRepository;
    private final AccountRepository accountRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<ConversationInboxDto> getInbox(UUID currentUserId, Pageable pageable) {
        Page<Conversation> conversations = conversationRepository
                .findByAccount1_AccountIdOrAccount2_AccountId(currentUserId, currentUserId, pageable);

        Set<UUID> conversationIds = conversations.getContent().stream()
                .map(Conversation::getConversationId)
                .collect(Collectors.toSet());

        Map<UUID, Long> unreadMap = conversationIds.isEmpty()
                ? Collections.emptyMap()
                : messagesRepository.countUnreadByConversationIds(currentUserId, conversationIds).stream()
                .collect(Collectors.toMap(ConversationUnreadCountProjection::getConversationId,
                        ConversationUnreadCountProjection::getUnreadCount));

        return conversations.map(conversation -> new ConversationInboxDto(
                conversation.getConversationId(),
                resolveOtherUserId(conversation, currentUserId),
                conversation.getConversationType(),
                conversation.getLastMessage() == null ? null : messageMapper.toPreviewDto(conversation.getLastMessage()),
                conversation.getLastMessageAt(),
                unreadMap.getOrDefault(conversation.getConversationId(), 0L)));
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalUnreadCount(UUID currentUserId) {
        return messagesRepository.countByReceiver_AccountIdAndReadFalseAndDeletedFalse(currentUserId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ConversationDetailDto> getConversationByUser(UUID currentUserId, UUID receiverId) {
        accountRepository.findById(receiverId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", receiverId));

        UUID[] ordered = orderParticipantIds(currentUserId, receiverId);
        return conversationRepository.findByAccount1_AccountIdAndAccount2_AccountId(ordered[0], ordered[1])
                .map(conversationMapper::toDetailDto);
    }

    @Override
    public void markConversationAsRead(UUID conversationId, UUID currentUserId) {
        Conversation conversation = getOwnedConversation(conversationId, currentUserId);
        int updated = messagesRepository.markAllAsRead(conversation.getConversationId(), currentUserId);
        log.debug("Marked {} messages as read for account {} in conversation {}", updated, currentUserId, conversationId);
    }

    private Conversation getOwnedConversation(UUID conversationId, UUID currentUserId) {
        return conversationRepository.findOwnedConversation(conversationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
    }

    private UUID resolveOtherUserId(Conversation conversation, UUID currentUserId) {
        if (conversation.getAccount1().getAccountId().equals(currentUserId)) {
            return conversation.getAccount2().getAccountId();
        }
        return conversation.getAccount1().getAccountId();
    }

    private UUID[] orderParticipantIds(UUID userId1, UUID userId2) {
        String id1 = userId1.toString();
        String id2 = userId2.toString();
        return id1.compareTo(id2) <= 0
                ? new UUID[] { userId1, userId2 }
                : new UUID[] { userId2, userId1 };
    }
}



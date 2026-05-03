package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.MessageDto;
import fpt.edu.sep490.pilahub.dto.request.message.SendMessageRequest;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.MessageMapper;
import fpt.edu.sep490.pilahub.enums.ConversationType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Conversation;
import fpt.edu.sep490.pilahub.pojo.Messages;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.ConversationRepository;
import fpt.edu.sep490.pilahub.repository.MessagesRepository;
import fpt.edu.sep490.pilahub.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessagesRepository messagesRepository;
    private final ConversationRepository conversationRepository;
    private final AccountRepository accountRepository;
    private final MessageMapper messageMapper;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Override
    @Transactional(readOnly = true)
    public Page<MessageDto> getConversationMessages(UUID conversationId, UUID currentUserId, Pageable pageable) {
        getOwnedConversation(conversationId, currentUserId);
        return messagesRepository.findByConversation_ConversationIdAndDeletedFalse(conversationId, pageable)
                .map(messageMapper::toDto);
    }

    @Override
    public MessageDto sendMessage(UUID senderId, SendMessageRequest request) {
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", senderId));

        Account receiver = accountRepository.findById(request.receiverId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", request.receiverId()));

        if (sender.getAccountId().equals(receiver.getAccountId())) {
            throw new IllegalArgumentException("You cannot send a message to yourself");
        }

        Conversation conversation = findOrCreateConversation(sender, receiver);

        Messages message = Messages.builder()
                .conversation(conversation)
                .sender(sender)
                .receiver(receiver)
                .content(request.content())
                .messageType(request.messageType())
                .build();

        Messages savedMessage = messagesRepository.save(message);
        conversation.setLastMessage(savedMessage);
        conversation.setLastMessageAt(savedMessage.getCreateAt());
        conversationRepository.save(conversation);

        MessageDto responseDto = messageMapper.toDto(savedMessage);
        publishMessageBestEffort(receiver, responseDto);
        return responseDto;
    }

    @Override
    public void deleteMessage(UUID messageId, UUID currentUserId) {
        Messages message = messagesRepository.findByMessageIdAndDeletedFalse(messageId)
                .orElseThrow(() -> new ResourceNotFoundException("Message", "id", messageId));

        if (!message.getSender().getAccountId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only revoke your own messages");
        }

        message.setDeleted(true);
        message.setDeletedAt(Instant.now());
        messagesRepository.save(message);

        Conversation conversation = message.getConversation();
        if (conversation.getLastMessage() != null && conversation.getLastMessage().getMessageId().equals(messageId)) {
            messagesRepository.findTopByConversation_ConversationIdAndDeletedFalseOrderByCreateAtDesc(conversation.getConversationId())
                    .ifPresentOrElse(latest -> {
                        conversation.setLastMessage(latest);
                        conversation.setLastMessageAt(latest.getCreateAt());
                    }, () -> {
                        conversation.setLastMessage(null);
                        conversation.setLastMessageAt(null);
                    });
            conversationRepository.save(conversation);
        }

        log.info("Message {} revoked by account {}", messageId, currentUserId);
    }

    private Conversation findOrCreateConversation(Account sender, Account receiver) {
        UUID[] ordered = orderParticipantIds(sender.getAccountId(), receiver.getAccountId());

        return conversationRepository.findByAccount1_AccountIdAndAccount2_AccountId(ordered[0], ordered[1])
                .orElseGet(() -> createConversationSafely(ordered[0], ordered[1]));
    }

    private Conversation createConversationSafely(UUID account1Id, UUID account2Id) {
        Account account1 = accountRepository.findById(account1Id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", account1Id));
        Account account2 = accountRepository.findById(account2Id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", account2Id));

        ConversationType conversationType = resolveConversationType(account1, account2);

        Conversation newConversation = Conversation.builder()
                .account1(account1)
                .account2(account2)
                .conversationType(conversationType)
                .build();

        try {
            return conversationRepository.save(newConversation);
        } catch (DataIntegrityViolationException ex) {
            return conversationRepository.findByAccount1_AccountIdAndAccount2_AccountId(account1Id, account2Id)
                    .orElseThrow(() -> ex);
        }
    }

    private ConversationType resolveConversationType(Account account1, Account account2) {
        Role role1 = account1.getRole();
        Role role2 = account2.getRole();

        if ((role1 == Role.TRAINEE && role2 == Role.COACH) || (role1 == Role.COACH && role2 == Role.TRAINEE)) {
            return ConversationType.TRAINEE_COACH;
        }

        if ((role1 == Role.TRAINEE && role2 == Role.VENDOR) || (role1 == Role.VENDOR && role2 == Role.TRAINEE)) {
            return ConversationType.TRAINEE_VENDOR;
        }

        throw new InvalidRequestException(
                String.format("Conversation is only supported between TRAINEE-COACH or TRAINEE-VENDOR. Current roles: %s and %s", role1, role2));
    }

    private Conversation getOwnedConversation(UUID conversationId, UUID currentUserId) {
        return conversationRepository.findOwnedConversation(conversationId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));
    }

    private UUID[] orderParticipantIds(UUID userId1, UUID userId2) {
        String id1 = userId1.toString();
        String id2 = userId2.toString();
        return id1.compareTo(id2) <= 0
                ? new UUID[] { userId1, userId2 }
                : new UUID[] { userId2, userId1 };
    }

    private void publishMessageBestEffort(Account receiver, MessageDto messageDto) {
        try {
            // Primary route by UUID for frontend convention.
            simpMessagingTemplate.convertAndSendToUser(
                    receiver.getAccountId().toString(),
                    "/queue/messages",
                    messageDto);

            // Compatibility route for existing modules where Principal name is email.
            simpMessagingTemplate.convertAndSendToUser(
                    receiver.getEmail(),
                    "/queue/messages",
                    messageDto);
        } catch (Exception ex) {
            // Real-time delivery is best-effort; persistence already succeeded.
            log.warn("WebSocket push failed for message {}: {}", messageDto.messageId(), ex.getMessage());
        }
    }
}


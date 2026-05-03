package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Messages;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessagesRepository extends JpaRepository<Messages, UUID> {

    Page<Messages> findByConversation_ConversationIdAndDeletedFalse(UUID conversationId, Pageable pageable);

    Optional<Messages> findByMessageIdAndDeletedFalse(UUID messageId);

    Optional<Messages> findTopByConversation_ConversationIdAndDeletedFalseOrderByCreateAtDesc(UUID conversationId);

    long countByReceiver_AccountIdAndReadFalseAndDeletedFalse(UUID receiverId);

    @Query("""
            select m.conversation.conversationId as conversationId,
                   count(m) as unreadCount
            from Messages m
            where m.receiver.accountId = :receiverId
              and m.read = false
              and m.deleted = false
              and m.conversation.conversationId in :conversationIds
            group by m.conversation.conversationId
            """)
    List<ConversationUnreadCountProjection> countUnreadByConversationIds(@Param("receiverId") UUID receiverId,
                                                                         @Param("conversationIds") Collection<UUID> conversationIds);

    @Modifying
    @Transactional
    @Query("""
            update Messages m
            set m.read = true
            where m.conversation.conversationId = :conversationId
              and m.receiver.accountId = :receiverId
              and m.read = false
              and m.deleted = false
            """)
    int markAllAsRead(@Param("conversationId") UUID conversationId, @Param("receiverId") UUID receiverId);
}


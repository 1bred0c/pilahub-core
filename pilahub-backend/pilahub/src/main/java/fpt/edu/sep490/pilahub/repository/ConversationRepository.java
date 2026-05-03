package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByAccount1_AccountIdAndAccount2_AccountId(UUID account1Id, UUID account2Id);

    Page<Conversation> findByAccount1_AccountIdOrAccount2_AccountId(UUID account1Id, UUID account2Id, Pageable pageable);

    @Query("""
            select c from Conversation c
            where c.conversationId = :conversationId
              and (c.account1.accountId = :accountId or c.account2.accountId = :accountId)
            """)
    Optional<Conversation> findOwnedConversation(@Param("conversationId") UUID conversationId,
                                                 @Param("accountId") UUID accountId);
}


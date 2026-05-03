package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.ConversationType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(name = "uk_conversation_account_pair", columnNames = { "account1_id", "account2_id" })
}, indexes = {
        @Index(name = "idx_conversation_account1", columnList = "account1_id"),
        @Index(name = "idx_conversation_account2", columnList = "account2_id"),
        @Index(name = "idx_conversation_last_message_at", columnList = "last_message_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue
    @Column(name = "conversation_id", nullable = false, updatable = false)
    private UUID conversationId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account1_id", nullable = false)
    private Account account1;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account2_id", nullable = false)
    private Account account2;

    @NotNull
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    @Column(name = "conversation_type", nullable = false, updatable = false, length = 30)
    private ConversationType conversationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    private Messages lastMessage;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

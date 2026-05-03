package fpt.edu.sep490.pilahub.pojo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fpt.edu.sep490.pilahub.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue
    @Column(name = "ticket_id", nullable = false, updatable = false)
    private UUID ticketId;

    @NotNull(message = "Account must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference("account-tickets")
    private Account account;

    @NotNull(message = "Ticket type must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    @JsonBackReference("ticket-type-tickets")
    private TicketType ticketType;

    @NotBlank(message = "Title must not be blank")
    @Size(max = 255)
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @NotBlank(message = "Description must not be blank")
    @Size(max = 2000)
    @Column(name = "description", nullable = false, length = 2000)
    private String description;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private TicketStatus status = TicketStatus.PENDING;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

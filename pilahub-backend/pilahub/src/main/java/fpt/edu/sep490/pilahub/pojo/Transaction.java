package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.TransactionType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue
    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @NotNull(message = "Transaction type must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 50)
    private TransactionType transactionType;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Account ID must not be null")
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Size(max = 500)
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "transaction_date", nullable = false, updatable = false)
    private Instant transactionDate;

    @PrePersist
    protected void onCreate() {
        this.transactionDate = Instant.now();
    }
}

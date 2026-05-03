package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_withdrawals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletWithdrawal {

    @Id
    @GeneratedValue
    @Column(name = "wallet_withdrawal_id", nullable = false, updatable = false)
    private UUID walletWithdrawalId;

    @NotNull(message = "Wallet must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Wallet wallet;

    @NotBlank(message = "Recipient name must not be blank")
    @Size(max = 255)
    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @NotBlank(message = "Bank account number must not be blank")
    @Size(max = 50)
    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @NotBlank(message = "Bank code must not be blank")
    @Size(max = 50)
    @Column(name = "bank_code", nullable = false, length = 50)
    private String bankCode;

    @NotBlank(message = "Bank name must not be blank")
    @Size(max = 255)
    @Column(name = "bank_name", nullable = false, length = 255)
    private String bankName;

    @Size(max = 500)
    @Column(name = "bank_logo", length = 500)
    private String bankLogo;

    @Size(max = 500)
    @Column(name = "receipt_url", length = 500)
    private String receiptUrl;

    @NotNull(message = "Amount must not be null")
    @DecimalMin(value = "10000.0", message = "Minimum withdrawal amount is 10,000 VND")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private WalletWithdrawalStatus status = WalletWithdrawalStatus.PENDING;

    @Size(max = 1000)
    @Column(name = "note", length = 1000)
    private String note;

    @Size(max = 1000)
    @Column(name = "admin_note", length = 1000)
    private String adminNote;

    @Column(name = "processed_by")
    private UUID processedBy;

    @NotNull
    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = Instant.now();
        }
        if (this.status == null) {
            this.status = WalletWithdrawalStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

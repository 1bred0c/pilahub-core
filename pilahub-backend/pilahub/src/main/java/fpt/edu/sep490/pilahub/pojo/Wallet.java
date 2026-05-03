package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @Column(name = "account_id", nullable = false, updatable = false)
    private UUID accountId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull(message = "Balance must not be null")
    @DecimalMin(value = "0.0", message = "Balance must not be negative")
    @Column(name = "balance_vnd", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal balanceVND = BigDecimal.ZERO;

    @NotNull(message = "Available balance must not be null")
    @DecimalMin(value = "0.0", message = "Available balance must not be negative")
    @Column(name = "available_vnd", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal availableVND = BigDecimal.ZERO;

    @NotNull(message = "Locked balance must not be null")
    @DecimalMin(value = "0.0", message = "Locked balance must not be negative")
    @Column(name = "locked_vnd", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal lockedVND = BigDecimal.ZERO;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @NotNull
    @Column(name = "open_at", nullable = false, updatable = false)
    private Instant openAt;


    @PrePersist
    protected void onCreate() {
        if (this.openAt == null) {
            this.openAt = Instant.now();
        }
    }
}

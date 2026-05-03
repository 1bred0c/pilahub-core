package fpt.edu.sep490.pilahub.pojo;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import fpt.edu.sep490.pilahub.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue
    @Column(name = "order_id", nullable = false, updatable = false)
    private UUID orderId;

    @NotNull(message = "Account must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @NotNull(message = "Order status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @NotNull(message = "Total amount must not be null")
    @DecimalMin(value = "0.0", message = "Total amount must not be negative")
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.0", message = "Discount amount must not be negative")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @NotNull(message = "Shipping fee must not be null")
    @DecimalMin(value = "0.0", message = "Shipping fee must not be negative")
    @Column(name = "shipping_fee", nullable = false, precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @NotBlank(message = "Recipient name must not be blank")
    @Size(max = 255)
    @Column(name = "recipient_name", nullable = false, length = 255)
    private String recipientName;

    @NotBlank(message = "Recipient phone must not be blank")
    @Size(max = 20)
    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @NotBlank(message = "Shipping address must not be blank")
    @Size(max = 500)
    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

    @Size(max = 255)
    @Column(name = "recipient_ward", length = 255)
    private String recipientWard;

    @Size(max = 255)
    @Column(name = "recipient_district", length = 255)
    private String recipientDistrict;

    @Size(max = 100)
    @Column(name = "order_number", unique = true, length = 100)
    private String orderNumber;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "is_paid", nullable = false)
    @Builder.Default
    private boolean paid = false;

    @Column(name = "is_paid_out", nullable = false)
    @Builder.Default
    private boolean paidOut = false;

    @Column(name = "paid_at")
    private Instant paidAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    /**
     * Exactly one shipment per order (each order already belongs to a single
     * vendor).
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-shipments")
    @Builder.Default
    private List<Shipment> shipments = new ArrayList<>();

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

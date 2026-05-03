package fpt.edu.sep490.pilahub.pojo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import fpt.edu.sep490.pilahub.enums.OrderDetailStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDetail {

    @Id
    @GeneratedValue
    @Column(name = "order_detail_id", nullable = false, updatable = false)
    private UUID orderDetailId;

    @NotNull(message = "Order must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference
    private Order order;

    @NotNull(message = "Product must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private OrderDetailStatus status = OrderDetailStatus.PENDING;

    @NotNull(message = "Quantity must not be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull(message = "Unit price must not be null")
    @DecimalMin(value = "0.0", message = "Unit price must not be negative")
    @Column(name = "unit_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @NotNull(message = "Subtotal must not be null")
    @DecimalMin(value = "0.0", message = "Subtotal must not be negative")
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @DecimalMin(value = "0.0", message = "Discount amount must not be negative")
    @Column(name = "discount_amount", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "installation_request", nullable = false)
    @Builder.Default
    private boolean installationRequest = false;

    @Size(max = 255)
    @Column(name = "product_name", length = 255)
    private String productName;

    @Size(max = 500)
    @Column(name = "product_image_url", length = 500)
    private String productImageUrl;

    /**
     * The vendor shipment this line item belongs to.
     * Null until the order is confirmed and shipments are created per vendor.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shipment_id")
    @JsonBackReference("shipment-orderdetails")
    private Shipment shipment;

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

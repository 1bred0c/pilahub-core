package fpt.edu.sep490.pilahub.pojo;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import fpt.edu.sep490.pilahub.enums.ShippingProvider;
import fpt.edu.sep490.pilahub.enums.ShipmentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "shipments", indexes = {
        @Index(name = "idx_shipment_order", columnList = "order_id"),
        @Index(name = "idx_shipment_vendor", columnList = "vendor_id"),
        @Index(name = "idx_shipment_tracking", columnList = "tracking_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue
    @Column(name = "shipment_id", nullable = false, updatable = false)
    private UUID shipmentId;

    // ── Relations ─────────────────────────────────────────────────────────────

    @NotNull(message = "Order must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    @JsonBackReference("order-shipments")
    private Order order;

    @NotNull(message = "Vendor must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    /**
     * All order-detail lines that belong to this vendor's shipment. Read-only
     * navigation — lifecycle is owned by Order.
     */
    @OneToMany(mappedBy = "shipment")
    @JsonManagedReference("shipment-orderdetails")
    @Builder.Default
    private List<OrderDetail> orderDetails = new ArrayList<>();

    // ── Status ────────────────────────────────────────────────────────────────

    @NotNull(message = "Shipment status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private ShipmentStatus status = ShipmentStatus.DRAFT;

    // ── Carrier / tracking ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "shipping_provider", length = 20)
    private ShippingProvider shippingProvider;

    @Size(max = 100)
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    // ── Key timestamps ────────────────────────────────────────────────────────

    @Column(name = "estimated_delivery_at")
    private Instant estimatedDeliveryAt;

    @Column(name = "shipped_at")
    private Instant shippedAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    // ── Post-delivery ─────────────────────────────────────────────────────────

    @Column(name = "return_deadline")
    private Instant returnDeadline;

    @Column(name = "payout_release_date")
    private Instant payoutReleaseDate;

    // ── Cancellation ──────────────────────────────────────────────────────────

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Size(max = 500)
    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    // ── Audit ─────────────────────────────────────────────────────────────────

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

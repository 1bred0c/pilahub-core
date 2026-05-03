package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vendors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {

    @Id
    @Column(name = "vendor_id", nullable = false, updatable = false)
    private UUID vendorId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "vendor_id")
    private Account account;

    @NotBlank(message = "Business name must not be blank")
    @Size(max = 255)
    @Column(name = "business_name", nullable = false, length = 255)
    private String businessName;

    @Size(max = 500)
    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Size(max = 20)
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Size(max = 500)
    @Column(name = "address", length = 500)
    private String address;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    private String city;

    @Size(max = 100)
    @Column(name = "country", length = 100)
    private String country;

    @Size(max = 500)
    @Column(name = "business_license_url", length = 500)
    private String businessLicenseUrl;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @DecimalMin(value = "0.0", message = "Platform fee percentage must be at least 0")
    @DecimalMax(value = "100.0", message = "Platform fee percentage must not exceed 100")
    @Column(name = "platform_fee_percentage")
    private Double platformFeePercentage;

    @Min(value = 0, message = "Holding days must be at least 0")
    @Column(name = "holding_days")
    private Integer holdingDays;

    /**
     * GHN Shop ID assigned when the vendor registers their store on GHN via
     * {@code POST /shiip/public-api/v2/shop/register}.
     * Null until the vendor completes GHN store registration.
     * Sent as the {@code ShopId} header on all per-vendor GHN API calls.
     */
    @Column(name = "ghn_shop_id")
    private Integer ghnShopId;

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

package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.CategoryType;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue
    @Column(name = "product_id", nullable = false, updatable = false)
    private UUID productId;

    @NotNull(message = "Vendor must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @NotNull(message = "Category must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NotBlank(message = "Product name must not be blank")
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @NotNull(message = "Price must not be null")
    @DecimalMin(value = "0.0", message = "Price must not be negative")
    @Column(name = "price", nullable = false)
    private Double price;

    @Min(value = 0, message = "Stock quantity must not be negative")
    @Column(name = "stock_quantity")
    private Integer stockQuantity;

    @Size(max = 100)
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 500)
    @Column(name = "specifications", length = 500)
    private String specifications;

    @Enumerated(EnumType.STRING)
    @Column(name = "category_type", length = 30)
    private CategoryType categoryType;

    @Column(name = "ref_id")
    private UUID refId;

    @Column(name = "expired_date")
    private Instant expiredDate;

    @Min(value = 1, message = "Package height must be at least 1 cm")
    @Column(name = "height")
    private Integer height;

    @Min(value = 1, message = "Package length must be at least 1 cm")
    @Column(name = "length")
    private Integer length;

    @Min(value = 1, message = "Package width must be at least 1 cm")
    @Column(name = "width")
    private Integer width;

    @Min(value = 1, message = "Package weight must be at least 1 gram")
    @Column(name = "weight")
    private Integer weight;

    @Column(name = "installation_supported", nullable = false)
    @Builder.Default
    private boolean installationSupported = false;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "region_supported", columnDefinition = "text[]")
    private String[] regionSupported;

    @Min(value = 0, message = "Average rating must not be negative")
    @Max(value = 5, message = "Average rating must not exceed 5")
    @Column(name = "avg_rating")
    private Double avgRating;

    @Min(value = 0, message = "Review count must not be negative")
    @Column(name = "review_count")
    @Builder.Default
    private Integer reviewCount = 0;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "rule_violation", nullable = false)
    @Builder.Default
    private boolean ruleViolation = false;

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

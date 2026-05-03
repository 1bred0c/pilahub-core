package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue
    @Column(name = "address_id", nullable = false, updatable = false)
    private UUID addressId;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotBlank(message = "Receiver name must not be blank")
    @Size(max = 255)
    @Column(name = "receiver_name", nullable = false, length = 255)
    private String receiverName;

    @NotBlank(message = "Receiver phone must not be blank")
    @Pattern(
            regexp = "^\\+?[0-9]{9,15}$",
            message = "Receiver phone format is invalid"
    )
    @Column(name = "receiver_phone", nullable = false, length = 20)
    private String receiverPhone;

    @NotBlank(message = "Address line must not be blank")
    @Size(max = 500)
    @Column(name = "address_line", nullable = false, length = 500)
    private String addressLine;

    @NotBlank(message = "Province must not be blank")
    @Size(max = 100)
    @Column(name = "province", nullable = false, length = 100)
    private String province;

    @NotBlank(message = "City must not be blank")
    @Size(max = 100)
    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @NotBlank(message = "District must not be blank")
    @Size(max = 100)
    @Column(name = "district", nullable = false, length = 100)
    private String district;

    @NotBlank(message = "Ward must not be blank")
    @Size(max = 100)
    @Column(name = "ward", nullable = false, length = 100)
    private String ward;

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

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

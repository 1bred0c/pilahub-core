package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "supplements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplement {

    @Id
    @GeneratedValue
    @Column(name = "supplement_id", nullable = false, updatable = false)
    private UUID supplementId;

    @NotBlank(message = "Supplement name must not be blank")
    @Size(max = 255)
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Size(max = 2000)
    @Column(name = "description", length = 2000)
    private String description;

    @Size(max = 100)
    @Column(name = "brand", length = 100)
    private String brand;

    @Size(max = 100)
    @Column(name = "form", length = 100)
    private String form;

    @Size(max = 500)
    @Column(name = "usage_instructions", length = 500)
    private String usageInstructions;

    @Size(max = 1000)
    @Column(name = "benefits", length = 1000)
    private String benefits;

    @Size(max = 500)
    @Column(name = "side_effects", length = 500)
    private String sideEffects;

    @Size(max = 500)
    @Column(name = "contraindications", length = 500)
    private String contraindications;

    @Size(max = 500)
    @Column(name = "warnings", length = 500)
    private String warnings;

    @Size(max = 1000)
    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

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

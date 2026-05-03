package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "purposes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Purpose {

    @Id
    @GeneratedValue
    @Column(name = "purpose_id", nullable = false, updatable = false)
    private UUID purposeId;

    @NotBlank(message = "Purpose name must not be blank")
    @Size(max = 255)
    @Column(name = "name", nullable = false, unique = true, length = 255)
    private String name;

    @NotBlank(message = "Purpose code must not be blank")
    @Size(max = 100)
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

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

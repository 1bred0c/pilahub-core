package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_injuries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalInjury {

    @Id
    @GeneratedValue
    @Column(name = "personal_injury_id", nullable = false, updatable = false)
    private UUID personalInjuryId;

    @NotNull(message = "Trainee must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotNull(message = "Injury must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "injury_id", nullable = false)
    private Injury injury;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private InjuryStatus status = InjuryStatus.ACTIVE;

    @Size(max = 1000)
    @Column(name = "notes", length = 1000)
    private String notes;

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

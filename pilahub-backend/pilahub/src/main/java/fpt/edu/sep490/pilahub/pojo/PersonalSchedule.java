package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "personal_schedules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalSchedule {

    @Id
    @GeneratedValue
    @Column(name = "personal_schedule_id", nullable = false, updatable = false)
    private UUID personalScheduleId;

    @NotNull(message = "Personal stage must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "personal_stage_id", nullable = false)
    private PersonalStage personalStage;

    @OneToMany(mappedBy = "personalSchedule")
    private List<PersonalExercise> exercises;

    @NotBlank(message = "Schedule name must not be blank")
    @Size(max = 255)
    @Column(name = "schedule_name", nullable = false, length = 255)
    private String scheduleName;

    @Size(max = 1000)
    @Column(name = "description", length = 1000)
    private String description;

    @Size(max = 20)
    @Column(name = "day_of_week", length = 20)
    private String dayOfWeek;

    @Column(name = "scheduled_date")
    private Instant scheduledDate;

    @Min(value = 1, message = "Duration must be at least 1")
    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

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

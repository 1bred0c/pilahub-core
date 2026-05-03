package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.CoachRoadmapRequestStatus;
import fpt.edu.sep490.pilahub.enums.WorkoutLevel;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "coach_roadmap_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoachRoadmapRequest {

    @Id
    @GeneratedValue
    @Column(name = "request_id", nullable = false, updatable = false)
    private UUID requestId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainee_id", nullable = false)
    private Trainee trainee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private CoachRoadmapRequestStatus status = CoachRoadmapRequestStatus.PENDING;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "primary_goal_id", nullable = false)
    private FitnessGoal primaryGoal;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coach_roadmap_request_secondary_goals", joinColumns = @JoinColumn(name = "request_id"))
    @Column(name = "goal_id")
    @Builder.Default
    private List<UUID> secondaryGoalIds = new ArrayList<>();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "workout_level", nullable = false, length = 30)
    private WorkoutLevel workoutLevel;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "coach_roadmap_request_training_days", joinColumns = @JoinColumn(name = "request_id"))
    @Builder.Default
    private List<TrainingDaySchedule> trainingDaySchedules = new ArrayList<>();

    @Transient
    public List<DayOfWeek> getTrainingDays() {
        if (trainingDaySchedules == null || trainingDaySchedules.isEmpty()) {
            return List.of();
        }
        return trainingDaySchedules.stream()
                .map(TrainingDaySchedule::getDayOfWeek)
                .toList();
    }

    @Min(value = 1, message = "Duration must be at least 1 week")
    @Max(value = 52, message = "Duration must not exceed 52 weeks")
    @Column(name = "duration_weeks")
    private Integer durationWeeks;

    @Size(max = 1000)
    @Column(name = "trainee_message", length = 1000)
    private String traineeMessage;

    @Size(max = 500)
    @Column(name = "coach_note", length = 500)
    private String coachNote;

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

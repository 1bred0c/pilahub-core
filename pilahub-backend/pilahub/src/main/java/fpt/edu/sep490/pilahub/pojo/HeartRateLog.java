package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "heart_rate_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeartRateLog {

    @Id
    @GeneratedValue
    @Column(name = "heart_rate_log_id", nullable = false, updatable = false)
    private UUID heartRateLogId;

    @NotNull(message = "Workout session must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_session_id", nullable = false)
    private WorkoutSession workoutSession;

    @NotNull(message = "Heart rate must not be null")
    @Min(value = 1, message = "Heart rate must be at least 1")
    @Column(name = "heart_rate", nullable = false)
    private int heartRate;

    @Column(name = "recorded_at")
    private Integer recordedAt; // Thời điểm ghi nhận nhịp tim, tính bằng giây kể từ khi bắt đầu phiên tập
}

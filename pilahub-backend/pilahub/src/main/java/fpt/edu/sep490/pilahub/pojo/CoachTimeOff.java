package fpt.edu.sep490.pilahub.pojo;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "coach_time_offs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Vì lịch của coach được cố định là từ 6h-20h nên đây sẽ là nơi để coach book thời gian nghỉ của mình, tránh bị book lịch trong khoảng thời gian này
// Rule là mỗi tuần không được nghỉ quá 8 giờ
// Và lịch nghỉ phải được book trước ít nhất 24h, book theo đơn vị là giờ
public class CoachTimeOff {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @NotNull(message = "Coach must not be null")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coach_id", nullable = false)
    private Coach coach;

    @NotNull(message = "Start time must not be null")
    @Column(name = "start_time", nullable = false)
    private Instant startTime;

    @NotNull(message = "End time must not be null")
    @Column(name = "end_time", nullable = false)
    private Instant endTime;

    @Size(max = 500)
    @Column(name = "reason", length = 500)
    private String reason;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

package fpt.edu.sep490.pilahub.pojo;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.Instant;
import java.util.UUID;


@Entity
@Table(name = "live_session_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSessionReport {
    @Id
    @Column(name = "live_session_id", nullable = false, updatable = false)
    private UUID liveSessionId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "live_session_id")
    private LiveSession liveSession;

    @NotNull(message = "Reporter ID must not be null")
    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId; // Id của người báo cáo (Trainee)

    @NotNull(message = "Reported user ID must not be null")
    @Column(name = "reported_user_id", nullable = false)
    private UUID reportedUserId; // Id của người bị báo cáo (Coach)

    @NotNull(message = "Reason must not be null")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reason", referencedColumnName = "code", nullable = false,
            foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ReportReason reason; // Lý do report

    @Column(name = "description", length = 1000)
    private String description; // Mô tả chi tiết (optional, required if reason = OTHER)

    @NotNull(message = "Created at must not be null")
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt; // null = chưa xử lý, != null = đã xử lý

    @Column(name = "resolved_by")
    private UUID resolvedBy; // Admin ID người xử lý report

    @Column(name = "internal_note", length = 2000)
    private String internalNote; // Ghi chú nội bộ (chỉ admin thấy)

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

package fpt.edu.sep490.pilahub.pojo;

import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

//Lớp này để lưu phiên học giữa Coach và Trainee, đây là phiên học 1-1, dùng chung PK với CoachBooking vì mỗi Booking chỉ được tạo ra 1 phiên học duy nhất.
@Entity
@Table(name = "live_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveSession {

    @Id
    @Column(name = "live_session_id", nullable = false, updatable = false)
    private UUID liveSessionId; // Sẽ dùng chung PK với CoachBooking vì mỗi Booking chỉ được tạo ra 1 phiên học duy nhất.

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "live_session_id", nullable = false)
    private CoachBooking coachBooking;

    // Agora Credentials
    // Agora - channelName có ngay, token sinh sau
    @NotNull(message = "Channel name must not be null")
    @Column(name = "channel_name", nullable = false, length = 255)
    private String channelName; // ✅ Có ngay khi tạo

    @NotNull(message = "Coach UID must not be null")
    @Column(name = "coach_uid", nullable = false)
    private Integer coachUid;    // ✅ Có ngay khi tạo

    @NotNull(message = "Trainee UID must not be null")
    @Column(name = "trainee_uid", nullable = false)
    private Integer traineeUid;  // ✅ Có ngay khi tạo

    @Column(name = "coach_token", length = 1000)
    private String coachToken;      // ❌ NULL lúc đầu, sinh khi gần giờ học

    @Column(name = "trainee_token", length = 1000)
    private String traineeToken;    // ❌ NULL lúc đầu, sinh khi gần giờ học

    @Column(name = "token_generated_at")
    private Instant tokenGeneratedAt; // Thêm field này để track

    @Column(name = "token_expires_at")
    private Instant tokenExpiresAt;

    // Session Tracking - ĐƠN GIẢN
    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private LiveSessionStatus status = LiveSessionStatus.PENDING;

    @Column(name = "coach_joined_at")
    private Instant coachJoinedAt;

    @Column(name = "trainee_joined_at")
    private Instant traineeJoinedAt;

    @Column(name = "session_ended_at")
    private Instant sessionEndedAt;

    // Recording - ĐƠN GIẢN HƠN
    @Column(name = "recording_enabled", nullable = false)
    @Builder.Default
    private boolean recordingEnabled = true;

    @Column(name = "agora_resource_id", length = 500)
    private String agoraResourceId; // Cần để stop recording

    @Column(name = "agora_recording_sid", length = 500)
    private String agoraRecordingSid; // Cần để stop recording

    @Column(name = "recording_url", length = 1000)
    private String recordingUrl; // URL cuối cùng từ S3

    @Column(name = "recording_expires_at")
    private Instant recordingExpiresAt; // +7 days từ khi upload xong

    // Feedback & Rating
    @DecimalMin(value = "0.5", message = "Rating must be at least 0.5")
    @DecimalMax(value = "5.0", message = "Rating must not exceed 5.0")
    @Column(name = "rating_by_trainee", precision = 2, scale = 1)
    private BigDecimal ratingByTrainee; // Trainee rating after session completed (0.5-5.0 in 0.5 increments)


    // Metadata
    @Column(name = "error_message", length = 2000)
    private String errorMessage;

    @NotNull
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }
}

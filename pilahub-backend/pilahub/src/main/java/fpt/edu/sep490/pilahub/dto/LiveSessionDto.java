package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Live session information for 1-1 coaching")
public record LiveSessionDto(
        @Schema(description = "Live session identifier (same as coach booking ID)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID liveSessionId,

        @Schema(description = "Coach booking details")
        CoachBookingDto coachBooking,

        @Schema(description = "Agora channel name", example = "session_123e4567")
        String channelName,

        @Schema(description = "Coach UID for Agora", example = "10001")
        Integer coachUid,

        @Schema(description = "Trainee UID for Agora", example = "20001")
        Integer traineeUid,

        @Schema(description = "Coach access token (only visible to coach)")
        String coachToken,

        @Schema(description = "Trainee access token (only visible to trainee)")
        String traineeToken,

        @Schema(description = "Token generation timestamp", example = "2026-03-03T14:00:00Z")
        Instant tokenGeneratedAt,

        @Schema(description = "Token expiration timestamp", example = "2026-03-03T15:00:00Z")
        Instant tokenExpiresAt,

        @Schema(description = "Session status", example = "ACTIVE")
        LiveSessionStatus status,

        @Schema(description = "Coach join timestamp", example = "2026-03-03T14:05:00Z")
        Instant coachJoinedAt,

        @Schema(description = "Trainee join timestamp", example = "2026-03-03T14:03:00Z")
        Instant traineeJoinedAt,

        @Schema(description = "Session end timestamp", example = "2026-03-03T15:00:00Z")
        Instant sessionEndedAt,

        @Schema(description = "Whether recording is enabled", example = "true")
        boolean recordingEnabled,

        @Schema(description = "Agora resource ID for recording")
        String agoraResourceId,

        @Schema(description = "Agora recording SID")
        String agoraRecordingSid,

        @Schema(description = "Recording file URL (available after session)", example = "https://s3.amazonaws.com/pilahub/recordings/session_123.mp4")
        String recordingUrl,

    @Schema(description = "Recording expiration timestamp", example = "2026-03-10T15:00:00Z")
    Instant recordingExpiresAt,

    @Schema(description = "Rating by trainee after session completed (0.5-5.0)", example = "4.5")
    BigDecimal ratingByTrainee,


    @Schema(description = "Error message if any")
    String errorMessage,

        @Schema(description = "Session creation timestamp", example = "2026-03-01T10:00:00Z")
        Instant createdAt
) {
}


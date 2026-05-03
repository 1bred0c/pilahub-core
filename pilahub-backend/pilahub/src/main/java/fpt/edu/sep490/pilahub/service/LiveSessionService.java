package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.LiveSessionDto;
import fpt.edu.sep490.pilahub.dto.LiveSessionTokenDto;

import java.util.List;
import java.util.UUID;

public interface LiveSessionService {

    /**
     * Create a live session for a coach booking (called when payment is completed)
     */
    LiveSessionDto createLiveSession(UUID bookingId);

    /**
     * Get live session by ID
     */
    LiveSessionDto getLiveSessionById(UUID liveSessionId);

    /**
     * Get live session by coach booking ID
     */
    LiveSessionDto getLiveSessionByBookingId(UUID bookingId);

    /**
     * Get all sessions for a coach
     */
    List<LiveSessionDto> getCoachSessions(UUID coachId);

    /**
     * Get all sessions for a trainee
     */
    List<LiveSessionDto> getTraineeSessions(UUID traineeId);

    /**
     * Get my sessions (coach or trainee)
     */
    List<LiveSessionDto> getMySessions();

    /**
     * Get Agora token for joining session (only when session is READY/ACTIVE)
     */
    LiveSessionTokenDto getMySessionToken(UUID liveSessionId);

    /**
     * Mark current user as joined the session
     * Auto-starts recording when first person joins
     */
    LiveSessionDto markAsJoined(UUID liveSessionId);

    /**
     * Mark current user as left the session
     * Auto-stops recording when both users have left (room empty)
     */
    LiveSessionDto markAsLeft(UUID liveSessionId);

    /**
     * End session manually
     */
    LiveSessionDto endSession(UUID liveSessionId);

    /**
     * Generate tokens for sessions that are about to start (called by scheduler)
     */
    void generateTokensForUpcomingSessions();

    /**
     * Update session status based on booking status (called by scheduler)
     */
    void updateSessionStatusFromBooking();

    /**
     * Activate a session when its booking becomes READY
     */
    void activateSessionForReadyBooking(UUID bookingId);

    /**
     * Check for no-show and mark sessions accordingly (called by scheduler)
     */
    void checkAndHandleNoShow();

    /**
     * Complete sessions that have ended (called by scheduler)
     */
    void completeEndedSessions();

    /**
     * Enable/disable recording for a session (optional - for testing)
     */
    LiveSessionDto toggleRecording(UUID liveSessionId, boolean enabled);

    /**
     * Submit trainee rating for completed session (one-time only)
     */
    LiveSessionDto submitTraineeRating(UUID liveSessionId, java.math.BigDecimal rating);

}


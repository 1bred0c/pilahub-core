package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.HeartRateDto;

import java.util.UUID;

/**
 * Service for handling real-time heart rate streaming in live sessions
 */
public interface HeartRateService {

    /**
     * Process and forward heart rate from trainee to coach
     * Validates that the sender is the trainee of the session
     *
     * @param heartRateDto Heart rate data from trainee
     * @param accountId ID of the account sending the heart rate (authenticated user)
     */
    void sendHeartRate(HeartRateDto heartRateDto, UUID accountId);

    /**
     * Validate that the account is authorized to send heart rate for this session
     * Must be the trainee of the live session
     *
     * @param liveSessionId Live session ID
     * @param accountId Account ID
     * @return Coach account ID to send heart rate to
     */
    UUID validateAndGetCoachId(UUID liveSessionId, UUID accountId);

    /**
     * Get account ID by email (for WebSocket authentication)
     *
     * @param email User email
     * @return Account ID
     */
    UUID getAccountIdByEmail(String email);
}



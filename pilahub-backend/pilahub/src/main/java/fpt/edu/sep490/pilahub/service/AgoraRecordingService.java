package fpt.edu.sep490.pilahub.service;

import java.util.UUID;

/**
 * Service interface for Agora Cloud Recording
 * Handles starting, stopping, and managing video recordings
 */
public interface AgoraRecordingService {

    /**
     * Start cloud recording for a live session
     * @param liveSessionId Live session ID
     * @return Resource ID and Recording SID from Agora
     */
    void startRecording(UUID liveSessionId);

    /**
     * Stop cloud recording for a live session
     * @param liveSessionId Live session ID
     */
    void stopRecording(UUID liveSessionId);

    /**
     * Query recording status
     * @param liveSessionId Live session ID
     * @return Recording status and file information
     */
    String getRecordingStatus(UUID liveSessionId);

    /**
     * Get presigned recording URL for playback
     * Generates secure download URL that expires in 2 hours
     * @param liveSessionId Live session ID
     * @return Presigned URL for video playback
     */
    String getRecordingUrl(UUID liveSessionId);
}

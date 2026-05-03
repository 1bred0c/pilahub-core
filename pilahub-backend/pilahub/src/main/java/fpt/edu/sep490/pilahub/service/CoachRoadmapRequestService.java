package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CoachRoadmapRequestDto;
import fpt.edu.sep490.pilahub.dto.request.RejectCoachRoadmapRequestRequest;
import fpt.edu.sep490.pilahub.dto.request.SendRoadmapRequestToCoachRequest;

import java.util.List;
import java.util.UUID;

public interface CoachRoadmapRequestService {

    /**
     * Trainee sends a roadmap creation request to a chosen coach.
     */
    CoachRoadmapRequestDto sendRequestToCoach(UUID traineeAccountId, SendRoadmapRequestToCoachRequest request);

    /**
     * Trainee views all their sent requests.
     */
    List<CoachRoadmapRequestDto> getMySentRequests(UUID traineeAccountId);

    /**
     * Trainee cancels a pending request.
     */
    void cancelRequest(UUID traineeAccountId, UUID requestId);

    /**
     * Coach views all requests they received.
     */
    List<CoachRoadmapRequestDto> getMyReceivedRequests(UUID coachAccountId);

    /**
     * Coach views only pending requests they received.
     */
    List<CoachRoadmapRequestDto> getMyPendingReceivedRequests(UUID coachAccountId);

    /**
     * Coach accepts a pending request.
     * Returns the full request dto (containing the AI-generation parameters) so the coach
     * can immediately call POST /api/roadmaps/ai-generate.
     */
    CoachRoadmapRequestDto acceptRequest(UUID coachAccountId, UUID requestId);

    /**
     * Coach rejects a pending request with an optional note.
     */
    void rejectRequest(UUID coachAccountId, UUID requestId, RejectCoachRoadmapRequestRequest request);
}

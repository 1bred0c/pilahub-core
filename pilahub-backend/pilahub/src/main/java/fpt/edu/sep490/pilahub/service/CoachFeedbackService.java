package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CoachFeedbackDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachFeedbackRequest;

import java.util.List;
import java.util.UUID;

public interface CoachFeedbackService {

    CoachFeedbackDto createFeedback(UUID traineeId, CreateCoachFeedbackRequest request);

    CoachFeedbackDto getById(UUID feedbackId);

    List<CoachFeedbackDto> getByCoachId(UUID coachId);

    List<CoachFeedbackDto> getByTraineeId(UUID traineeId);

    Double getAverageRatingByCoachId(UUID coachId);

    CoachFeedbackDto updateFeedback(UUID feedbackId, UUID traineeId, UpdateCoachFeedbackRequest request);

    void deleteFeedback(UUID feedbackId, UUID traineeId);
}

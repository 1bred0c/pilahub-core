package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SessionAssessmentDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.SubmitSessionAssessmentRequest;

import java.util.List;
import java.util.UUID;

public interface SessionAssessmentService {

    SessionAssessmentDto submitAssessment(UUID liveSessionId, SubmitSessionAssessmentRequest request);

    SessionAssessmentDto getSessionAssessment(UUID liveSessionId);

    List<SessionAssessmentDto> getMyAssessmentHistory();
}


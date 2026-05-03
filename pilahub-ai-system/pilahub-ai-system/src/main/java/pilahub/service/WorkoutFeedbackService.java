package pilahub.service;

import pilahub.dto.request.WorkoutFeedbackAIRequest;
import pilahub.dto.request.WorkoutFeedbackAnalysisRequest;
import pilahub.dto.response.WorkoutFeedbackAnalysisResponse;

public interface WorkoutFeedbackService {
    /**
     * Analyzes workout performance and generates detailed feedback in Vietnamese
     * @param request The workout feedback analysis request (OLD - for backward compatibility)
     * @return Detailed workout feedback response
     */
    @Deprecated
    WorkoutFeedbackAnalysisResponse analyzeWorkoutFeedback(WorkoutFeedbackAnalysisRequest request);

    /**
     * Analyzes workout performance using new input structure from Backend Server
     * with File Search integration for reference guidelines
     * @param request The workout feedback AI request (NEW - from Backend Server)
     * @return Detailed workout feedback response
     */
    WorkoutFeedbackAnalysisResponse analyzeWorkoutFeedbackAI(WorkoutFeedbackAIRequest request);
}


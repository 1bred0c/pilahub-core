package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.WorkoutFeedbackDto;

import java.util.List;
import java.util.UUID;

public interface WorkoutFeedbackService {

    /**
     * Generate AI-powered feedback for a workout session
     * @param workoutSessionId The workout session ID
     * @param traineeId The trainee ID (for authorization)
     * @return Generated feedback DTO
     */
    WorkoutFeedbackDto generateFeedback(UUID workoutSessionId, UUID traineeId);

    /**
     * Get feedback by workout session ID
     * @param workoutSessionId The workout session ID
     * @param traineeId The trainee ID (for authorization)
     * @return Feedback DTO
     */
    WorkoutFeedbackDto getFeedbackByWorkoutSessionId(UUID workoutSessionId, UUID traineeId);

    /**
     * Get all feedback for a trainee
     * @param traineeId The trainee ID
     * @return List of feedback DTOs
     */
    List<WorkoutFeedbackDto> getAllFeedbackByTraineeId(UUID traineeId);

    /**
     * Delete feedback by workout session ID
     * @param workoutSessionId The workout session ID
     * @param traineeId The trainee ID (for authorization)
     */
    void deleteFeedback(UUID workoutSessionId, UUID traineeId);
}


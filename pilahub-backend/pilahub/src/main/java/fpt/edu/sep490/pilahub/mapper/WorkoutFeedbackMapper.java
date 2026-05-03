package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.WorkoutFeedbackDto;
import fpt.edu.sep490.pilahub.pojo.WorkoutFeedback;
import org.springframework.stereotype.Component;

@Component
public class WorkoutFeedbackMapper {

    public WorkoutFeedbackDto toDto(WorkoutFeedback workoutFeedback) {
        if (workoutFeedback == null) {
            return null;
        }

        return new WorkoutFeedbackDto(
                workoutFeedback.getWorkoutFeedbackId(),
                workoutFeedback.getWorkoutSession().getWorkoutSessionId(),
                workoutFeedback.getTotalMistakes(),
                workoutFeedback.getFormScore(),
                workoutFeedback.getEnduranceScore(),
                workoutFeedback.getOverallScore(),
                workoutFeedback.getStrengths(),
                workoutFeedback.getWeaknesses(),
                workoutFeedback.getRecommendations(),
                workoutFeedback.getAiModel(),
                workoutFeedback.getGeneratedAt()
        );
    }
}


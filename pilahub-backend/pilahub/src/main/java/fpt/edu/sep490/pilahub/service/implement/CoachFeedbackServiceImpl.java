package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachFeedbackDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CoachFeedbackMapper;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.CoachFeedback;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.CoachFeedbackRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.CoachFeedbackService;
import fpt.edu.sep490.pilahub.service.CoachService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoachFeedbackServiceImpl implements CoachFeedbackService {

    private final CoachFeedbackRepository coachFeedbackRepository;
    private final CoachRepository coachRepository;
    private final TraineeRepository traineeRepository;
    private final CoachFeedbackMapper coachFeedbackMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public CoachFeedbackDto createFeedback(UUID traineeId, CreateCoachFeedbackRequest request) {
        log.info("Creating feedback for coach ID: {} from trainee ID: {}", request.coachId(), traineeId);

        Coach coach = coachRepository.findById(request.coachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", request.coachId()));

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeId));

        // Check if trainee has already given feedback to this coach
        if (coachFeedbackRepository.existsByCoach_CoachIdAndTrainee_TraineeId(request.coachId(), traineeId)) {
            throw new IllegalStateException("You have already given feedback to this coach");
        }

        CoachFeedback feedback = coachFeedbackMapper.toEntity(request);
        feedback.setCoach(coach);
        feedback.setTrainee(trainee);

        CoachFeedback saved = coachFeedbackRepository.save(feedback);
        log.info("Successfully created feedback with ID: {}", saved.getFeedbackId());

        // Update coach's average rating
        calculateAndUpdateAverageRating(coach.getCoachId());

        eventPublisher.publishEvent(new NotificationEvent(
                this,
                coach.getCoachId(),
                NotificationType.COACH_FEEDBACK_RECEIVED,
                "Đã Nhận Đánh Giá Mới",
                trainee.getFullName() + " đã cho bạn đánh giá " + request.rating() + " sao.",
                saved.getFeedbackId(), "COACH_FEEDBACK"));

        return coachFeedbackMapper.toDto(saved);
    }

    @Override
    public CoachFeedbackDto getById(UUID feedbackId) {
        log.info("Fetching feedback by ID: {}", feedbackId);

        CoachFeedback feedback = coachFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach Feedback", "id", feedbackId));

        return coachFeedbackMapper.toDto(feedback);
    }

    @Override
    public List<CoachFeedbackDto> getByCoachId(UUID coachId) {
        log.info("Fetching all feedbacks for coach ID: {}", coachId);

        return coachFeedbackRepository.findByCoach_CoachId(coachId).stream()
                .map(coachFeedbackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachFeedbackDto> getByTraineeId(UUID traineeId) {
        log.info("Fetching all feedbacks from trainee ID: {}", traineeId);

        return coachFeedbackRepository.findByTrainee_TraineeId(traineeId).stream()
                .map(coachFeedbackMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Double getAverageRatingByCoachId(UUID coachId) {
        log.info("Calculating average rating for coach ID: {}", coachId);

        List<CoachFeedback> feedbacks = coachFeedbackRepository.findByCoach_CoachId(coachId);

        if (feedbacks.isEmpty()) {
            return 0.0;
        }

        double average = feedbacks.stream()
                .mapToInt(CoachFeedback::getRating)
                .average()
                .orElse(0.0);

        log.info("Average rating for coach ID {}: {}", coachId, average);
        return average;
    }

    @Override
    public CoachFeedbackDto updateFeedback(UUID feedbackId, UUID traineeId, UpdateCoachFeedbackRequest request) {
        log.info("Updating feedback ID: {} by trainee ID: {}", feedbackId, traineeId);

        CoachFeedback feedback = coachFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach Feedback", "id", feedbackId));

        // Verify that the feedback belongs to the trainee
        if (!feedback.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only update your own feedback");
        }

        coachFeedbackMapper.updateEntityFromRequest(request, feedback);

        CoachFeedback updated = coachFeedbackRepository.save(feedback);
        log.info("Successfully updated feedback with ID: {}", feedbackId);

        // Update coach's average rating
        calculateAndUpdateAverageRating(feedback.getCoach().getCoachId());

        return coachFeedbackMapper.toDto(updated);
    }

    @Override
    public void deleteFeedback(UUID feedbackId, UUID traineeId) {
        log.info("Deleting feedback ID: {} by trainee ID: {}", feedbackId, traineeId);

        CoachFeedback feedback = coachFeedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach Feedback", "id", feedbackId));

        // Verify that the feedback belongs to the trainee
        if (!feedback.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only delete your own feedback");
        }

        UUID coachId = feedback.getCoach().getCoachId();
        coachFeedbackRepository.delete(feedback);
        log.info("Successfully deleted feedback with ID: {}", feedbackId);

        // Update coach's average rating
        calculateAndUpdateAverageRating(coachId);
    }

    private void calculateAndUpdateAverageRating(UUID coachId) {

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        Double avgRating = coachFeedbackRepository.calculateAverageRatingByCoachId(coachId);

        // Set avgRating to null if there are no feedbacks, otherwise round to 2 decimal
        // places
        if (avgRating != null) {
            coach.setAvgRating(Math.round(avgRating * 100.0) / 100.0);
        } else {
            coach.setAvgRating(null);
        }

        coachRepository.save(coach);
    }
}

package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachRoadmapRequestDto;
import fpt.edu.sep490.pilahub.dto.TrainingDayScheduleDto;
import fpt.edu.sep490.pilahub.dto.request.RejectCoachRoadmapRequestRequest;
import fpt.edu.sep490.pilahub.dto.request.SendRoadmapRequestToCoachRequest;
import fpt.edu.sep490.pilahub.enums.CoachRoadmapRequestStatus;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CoachRoadmapRequestMapper;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.CoachRoadmapRequest;
import fpt.edu.sep490.pilahub.pojo.FitnessGoal;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.pojo.TrainingDaySchedule;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.CoachRoadmapRequestRepository;
import fpt.edu.sep490.pilahub.repository.FitnessGoalRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.CoachRoadmapRequestService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoachRoadmapRequestServiceImpl implements CoachRoadmapRequestService {

    private final CoachRoadmapRequestRepository coachRoadmapRequestRepository;
    private final TraineeRepository traineeRepository;
    private final CoachRepository coachRepository;
    private final FitnessGoalRepository fitnessGoalRepository;
    private final CoachRoadmapRequestMapper coachRoadmapRequestMapper;

    @Override
    public CoachRoadmapRequestDto sendRequestToCoach(UUID traineeAccountId, SendRoadmapRequestToCoachRequest request) {
        log.info("Trainee {} sending roadmap request to coach {}", traineeAccountId, request.coachId());

        Trainee trainee = traineeRepository.findById(traineeAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", traineeAccountId));

        Coach coach = coachRepository.findById(request.coachId())
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", request.coachId()));

        if (!coach.isActive()) {
            throw new IllegalStateException("Cannot send a request to an inactive coach");
        }

        boolean alreadyActive = coachRoadmapRequestRepository
                .existsByTrainee_TraineeIdAndCoach_CoachIdAndStatusIn(
                        traineeAccountId,
                        request.coachId(),
                        List.of(CoachRoadmapRequestStatus.PENDING));
        if (alreadyActive) {
            throw new IllegalStateException("You already have an active request for this coach");
        }

        FitnessGoal primaryGoal = fitnessGoalRepository.findById(request.primaryGoalId())
                .orElseThrow(() -> new ResourceNotFoundException("FitnessGoal", "id", request.primaryGoalId()));

        List<UUID> secondaryGoalIds = request.secondaryGoalIds() != null
                ? new ArrayList<>(request.secondaryGoalIds())
                : new ArrayList<>();

        for (UUID goalId : secondaryGoalIds) {
            if (goalId.equals(request.primaryGoalId())) {
                throw new IllegalArgumentException("Secondary goals must not contain the primary goal");
            }
            if (!fitnessGoalRepository.existsById(goalId)) {
                throw new ResourceNotFoundException("FitnessGoal", "id", goalId);
            }
        }

        long distinctTrainingDays = request.trainingDaySchedules().stream()
                .map(s -> s.dayOfWeek().name())
                .distinct()
                .count();
        if (distinctTrainingDays != request.trainingDaySchedules().size()) {
            throw new IllegalArgumentException("Training day schedules must not contain duplicate days");
        }

        List<TrainingDaySchedule> trainingDaySchedules = request.trainingDaySchedules().stream()
                .map(item -> TrainingDaySchedule.builder()
                        .dayOfWeek(item.dayOfWeek())
                        .startTime(item.startTime())
                        .build())
                .collect(Collectors.toCollection(ArrayList::new));

        CoachRoadmapRequest entity = CoachRoadmapRequest.builder()
                .trainee(trainee)
                .coach(coach)
                .status(CoachRoadmapRequestStatus.PENDING)
                .primaryGoal(primaryGoal)
                .secondaryGoalIds(secondaryGoalIds)
                .workoutLevel(request.workoutLevel())
                .trainingDaySchedules(trainingDaySchedules)
                .durationWeeks(request.durationWeeks())
                .traineeMessage(request.traineeMessage())
                .build();

        CoachRoadmapRequest saved = coachRoadmapRequestRepository.save(entity);
        log.info("Roadmap request {} created successfully", saved.getRequestId());

        return toDto(saved);
    }

    @Override
    public List<CoachRoadmapRequestDto> getMySentRequests(UUID traineeAccountId) {
        log.info("Fetching sent requests for trainee {}", traineeAccountId);

        return coachRoadmapRequestRepository
                .findByTrainee_TraineeIdOrderByCreatedAtDesc(traineeAccountId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public void cancelRequest(UUID traineeAccountId, UUID requestId) {
        log.info("Trainee {} cancelling request {}", traineeAccountId, requestId);

        CoachRoadmapRequest request = coachRoadmapRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachRoadmapRequest", "id", requestId));

        if (!request.getTrainee().getTraineeId().equals(traineeAccountId)) {
            throw new IllegalStateException("You can only cancel your own requests");
        }

        if (request.getStatus() != CoachRoadmapRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be cancelled");
        }

        request.setStatus(CoachRoadmapRequestStatus.CANCELLED);
        coachRoadmapRequestRepository.save(request);
        log.info("Request {} cancelled", requestId);
    }

    @Override
    public List<CoachRoadmapRequestDto> getMyReceivedRequests(UUID coachAccountId) {
        log.info("Fetching all received requests for coach {}", coachAccountId);

        return coachRoadmapRequestRepository
                .findByCoach_CoachIdOrderByCreatedAtDesc(coachAccountId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public List<CoachRoadmapRequestDto> getMyPendingReceivedRequests(UUID coachAccountId) {
        log.info("Fetching pending received requests for coach {}", coachAccountId);

        return coachRoadmapRequestRepository
                .findByCoach_CoachIdAndStatusOrderByCreatedAtDesc(coachAccountId, CoachRoadmapRequestStatus.PENDING)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    public CoachRoadmapRequestDto acceptRequest(UUID coachAccountId, UUID requestId) {
        log.info("Coach {} accepting request {}", coachAccountId, requestId);

        CoachRoadmapRequest request = coachRoadmapRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachRoadmapRequest", "id", requestId));

        if (!request.getCoach().getCoachId().equals(coachAccountId)) {
            throw new IllegalStateException("You can only accept requests directed to you");
        }

        if (request.getStatus() != CoachRoadmapRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be accepted");
        }

        request.setStatus(CoachRoadmapRequestStatus.ACCEPTED);
        CoachRoadmapRequest updated = coachRoadmapRequestRepository.save(request);
        log.info("Request {} accepted by coach {}", requestId, coachAccountId);

        return toDto(updated);
    }

    @Override
    public void rejectRequest(UUID coachAccountId, UUID requestId, RejectCoachRoadmapRequestRequest rejectRequest) {
        log.info("Coach {} rejecting request {}", coachAccountId, requestId);

        CoachRoadmapRequest request = coachRoadmapRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachRoadmapRequest", "id", requestId));

        if (!request.getCoach().getCoachId().equals(coachAccountId)) {
            throw new IllegalStateException("You can only reject requests directed to you");
        }

        if (request.getStatus() != CoachRoadmapRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be rejected");
        }

        request.setStatus(CoachRoadmapRequestStatus.REJECTED);
        request.setCoachNote(rejectRequest != null ? rejectRequest.coachNote() : null);
        coachRoadmapRequestRepository.save(request);
        log.info("Request {} rejected by coach {}", requestId, coachAccountId);
    }

    private CoachRoadmapRequestDto toDto(CoachRoadmapRequest entity) {
        CoachRoadmapRequestDto base = coachRoadmapRequestMapper.toDto(entity);
        List<String> secondaryGoalNames = Collections.emptyList();
        if (entity.getSecondaryGoalIds() != null && !entity.getSecondaryGoalIds().isEmpty()) {
            Map<UUID, String> nameMap = fitnessGoalRepository.findAllById(entity.getSecondaryGoalIds())
                    .stream()
                    .collect(Collectors.toMap(FitnessGoal::getGoalId, FitnessGoal::getVietnameseName));
            secondaryGoalNames = entity.getSecondaryGoalIds().stream()
                    .map(id -> nameMap.getOrDefault(id, null))
                    .toList();
        }

        List<TrainingDayScheduleDto> trainingDaySchedules = entity.getTrainingDaySchedules() == null
                ? Collections.emptyList()
                : entity.getTrainingDaySchedules().stream()
                        .map(coachRoadmapRequestMapper::toDto)
                        .toList();

        return new CoachRoadmapRequestDto(
                base.requestId(), base.traineeId(), base.traineeFullName(), base.traineeAvatarUrl(),
                base.coachId(), base.coachFullName(), base.status(),
                base.primaryGoalId(), base.primaryGoalName(),
                base.secondaryGoalIds(), secondaryGoalNames,
                base.workoutLevel(), base.trainingDays(), trainingDaySchedules, base.durationWeeks(),
                base.traineeMessage(), base.coachNote(), base.createdAt(), base.updatedAt());
    }
}

package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.MistakeLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchMistakeLogsRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.MistakeLogRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.MistakeLogMapper;
import fpt.edu.sep490.pilahub.pojo.BodyPart;
import fpt.edu.sep490.pilahub.pojo.MistakeLog;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import fpt.edu.sep490.pilahub.repository.BodyPartRepository;
import fpt.edu.sep490.pilahub.repository.MistakeLogRepository;
import fpt.edu.sep490.pilahub.repository.WorkoutSessionRepository;
import fpt.edu.sep490.pilahub.service.MistakeLogService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MistakeLogServiceImpl implements MistakeLogService {

    private final MistakeLogRepository mistakeLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final BodyPartRepository bodyPartRepository;
    private final MistakeLogMapper mistakeLogMapper;
    private final SecurityUtil securityUtil;

    @Override
    public List<MistakeLogDto> getByWorkoutSessionId(UUID workoutSessionId) {
        // Verify workout session exists and belongs to current trainee
        UUID traineeId = securityUtil.getCurrentUserId();
        var workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only view mistake logs from your own workout sessions");
        }

        return mistakeLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtSecondAsc(workoutSessionId).stream()
                .map(mistakeLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public MistakeLogDto getById(UUID mistakeLogId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        MistakeLog mistakeLog = mistakeLogRepository.findById(mistakeLogId)
                .orElseThrow(() -> new ResourceNotFoundException("MistakeLog", "id", mistakeLogId));

        // Verify ownership
        if (!mistakeLog.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only view your own mistake logs");
        }

        return mistakeLogMapper.toDto(mistakeLog);
    }

    @Override
    public List<MistakeLogDto> batchCreate(BatchMistakeLogsRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();

        // Verify workout session exists and belongs to current trainee
        WorkoutSession workoutSession = workoutSessionRepository.findById(request.workoutSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", request.workoutSessionId()));

        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only add mistake logs to your own workout sessions");
        }

        // Verify workout session has AI tracking enabled
        if (!workoutSession.isHaveAITracking()) {
            throw new IllegalStateException("Cannot add mistake logs when AI tracking is not enabled for this workout session");
        }

        // Create mistake logs
        List<MistakeLog> mistakeLogs = new ArrayList<>();
        for (MistakeLogRequest mistakeRequest : request.mistakeLogs()) {
            BodyPart bodyPart = null;
            if (mistakeRequest.bodyPartId() != null) {
                bodyPart = bodyPartRepository.findById(mistakeRequest.bodyPartId())
                        .orElse(null);
            }

            MistakeLog mistakeLog = MistakeLog.builder()
                    .workoutSession(workoutSession)
                    .bodyPart(bodyPart)
                    .details(mistakeRequest.details())
                    .imageUrl(mistakeRequest.imageUrl())
                    .recordedAtSecond(mistakeRequest.recordedAtSecond())
                    .duration(mistakeRequest.duration())
                    .build();
            mistakeLogs.add(mistakeLog);
        }

        // Batch save
        List<MistakeLog> savedLogs = mistakeLogRepository.saveAll(mistakeLogs);

        return savedLogs.stream()
                .map(mistakeLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID mistakeLogId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        MistakeLog mistakeLog = mistakeLogRepository.findById(mistakeLogId)
                .orElseThrow(() -> new ResourceNotFoundException("MistakeLog", "id", mistakeLogId));

        // Verify ownership
        if (!mistakeLog.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only delete your own mistake logs");
        }

        mistakeLogRepository.deleteById(mistakeLogId);
    }
}


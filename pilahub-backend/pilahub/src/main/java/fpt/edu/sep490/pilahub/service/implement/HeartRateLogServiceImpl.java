package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.HeartRateLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchHeartRateLogsRequest;
import fpt.edu.sep490.pilahub.dto.request.workout.HeartRateLogRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.HeartRateLogMapper;
import fpt.edu.sep490.pilahub.pojo.HeartRateLog;
import fpt.edu.sep490.pilahub.pojo.WorkoutSession;
import fpt.edu.sep490.pilahub.repository.HeartRateLogRepository;
import fpt.edu.sep490.pilahub.repository.WorkoutSessionRepository;
import fpt.edu.sep490.pilahub.service.HeartRateLogService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HeartRateLogServiceImpl implements HeartRateLogService {

    private final HeartRateLogRepository heartRateLogRepository;
    private final WorkoutSessionRepository workoutSessionRepository;
    private final HeartRateLogMapper heartRateLogMapper;
    private final SecurityUtil securityUtil;

    @Override
    public List<HeartRateLogDto> getByWorkoutSessionId(UUID workoutSessionId) {
        // Verify workout session exists and belongs to current trainee
        UUID traineeId = securityUtil.getCurrentUserId();
        var workoutSession = workoutSessionRepository.findById(workoutSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", workoutSessionId));

        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only view heart rate logs from your own workout sessions");
        }

        return heartRateLogRepository.findByWorkoutSession_WorkoutSessionIdOrderByRecordedAtAsc(workoutSessionId).stream()
                .map(heartRateLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public HeartRateLogDto getById(UUID heartRateLogId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HeartRateLog heartRateLog = heartRateLogRepository.findById(heartRateLogId)
                .orElseThrow(() -> new ResourceNotFoundException("HeartRateLog", "id", heartRateLogId));

        // Verify ownership
        if (!heartRateLog.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only view your own heart rate logs");
        }

        return heartRateLogMapper.toDto(heartRateLog);
    }

    @Override
    public List<HeartRateLogDto> batchCreate(BatchHeartRateLogsRequest request) {
        // Get current trainee
        UUID traineeId = securityUtil.getCurrentUserId();

        // Verify workout session exists and belongs to current trainee
        WorkoutSession workoutSession = workoutSessionRepository.findById(request.workoutSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("WorkoutSession", "id", request.workoutSessionId()));

        if (!workoutSession.getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only add heart rate logs to your own workout sessions");
        }

        // Verify workout session has IoT device tracking enabled
        if (!workoutSession.isHaveIOTDeviceTracking()) {
            throw new IllegalStateException("Cannot add heart rate logs when IoT device tracking is not enabled for this workout session");
        }

        // Create heart rate logs
        List<HeartRateLog> heartRateLogs = new ArrayList<>();
        for (HeartRateLogRequest hrRequest : request.heartRateLogs()) {
            HeartRateLog heartRateLog = HeartRateLog.builder()
                    .workoutSession(workoutSession)
                    .heartRate(hrRequest.heartRate())
                    .recordedAt(hrRequest.recordedAt())
                    .build();
            heartRateLogs.add(heartRateLog);
        }

        // Batch save
        List<HeartRateLog> savedLogs = heartRateLogRepository.saveAll(heartRateLogs);

        return savedLogs.stream()
                .map(heartRateLogMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(UUID heartRateLogId) {
        UUID traineeId = securityUtil.getCurrentUserId();
        HeartRateLog heartRateLog = heartRateLogRepository.findById(heartRateLogId)
                .orElseThrow(() -> new ResourceNotFoundException("HeartRateLog", "id", heartRateLogId));

        // Verify ownership
        if (!heartRateLog.getWorkoutSession().getTrainee().getTraineeId().equals(traineeId)) {
            throw new IllegalStateException("You can only delete your own heart rate logs");
        }

        heartRateLogRepository.deleteById(heartRateLogId);
    }
}


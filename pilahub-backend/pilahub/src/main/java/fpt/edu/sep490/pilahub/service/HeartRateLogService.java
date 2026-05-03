package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.HeartRateLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchHeartRateLogsRequest;

import java.util.List;
import java.util.UUID;

public interface HeartRateLogService {

    List<HeartRateLogDto> getByWorkoutSessionId(UUID workoutSessionId);

    HeartRateLogDto getById(UUID heartRateLogId);

    List<HeartRateLogDto> batchCreate(BatchHeartRateLogsRequest request);

    void deleteById(UUID heartRateLogId);
}


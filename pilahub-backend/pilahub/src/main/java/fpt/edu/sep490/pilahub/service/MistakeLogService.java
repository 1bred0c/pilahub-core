package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.MistakeLogDto;
import fpt.edu.sep490.pilahub.dto.request.workout.BatchMistakeLogsRequest;

import java.util.List;
import java.util.UUID;

public interface MistakeLogService {

    List<MistakeLogDto> getByWorkoutSessionId(UUID workoutSessionId);

    MistakeLogDto getById(UUID mistakeLogId);

    List<MistakeLogDto> batchCreate(BatchMistakeLogsRequest request);

    void deleteById(UUID mistakeLogId);
}


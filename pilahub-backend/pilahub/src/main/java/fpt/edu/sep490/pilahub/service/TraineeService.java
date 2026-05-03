package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TraineeDto;
import fpt.edu.sep490.pilahub.dto.request.CreateTraineeRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateTraineeRequest;

import java.util.List;
import java.util.UUID;

public interface TraineeService {

    TraineeDto createTrainee(UUID accountId, CreateTraineeRequest request);

    TraineeDto getTraineeByAccountId(UUID accountId);

    TraineeDto getTraineeById(UUID traineeId);

    List<TraineeDto> getAllTrainees();

    TraineeDto updateTrainee(UUID accountId, UpdateTraineeRequest request);

    TraineeDto updateTraineeByAdmin(UUID traineeId, UpdateTraineeRequest request);

    void deleteTrainee(UUID traineeId);
}

package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.FitnessGoalDto;
import fpt.edu.sep490.pilahub.dto.request.CreateFitnessGoalRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateFitnessGoalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FitnessGoalService {

    FitnessGoalDto create(CreateFitnessGoalRequest request);

    FitnessGoalDto getById(UUID goalId);

    Page<FitnessGoalDto> getAll(Pageable pageable);

    List<FitnessGoalDto> getAllActive();

    List<FitnessGoalDto> search(String keyword);

    FitnessGoalDto update(UUID goalId, UpdateFitnessGoalRequest request);

    void deactivate(UUID goalId);

    void activate(UUID goalId);
}

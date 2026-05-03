package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.StageDto;
import fpt.edu.sep490.pilahub.dto.request.CreateStageRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateStageRequest;

import java.util.List;
import java.util.UUID;

public interface StageService {

    StageDto createStage(CreateStageRequest request);

    StageDto getById(UUID stageId);

    List<StageDto> getAll();

    List<StageDto> getAllActive();

    List<StageDto> searchByName(String name);

    StageDto updateStage(UUID stageId, UpdateStageRequest request);

    void deactivateStage(UUID stageId);

    void activateStage(UUID stageId);

    void deleteStage(UUID stageId);
}

package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PersonalStageDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageRequest;

import java.util.List;
import java.util.UUID;

public interface PersonalStageService {

    PersonalStageDto createStage(CreatePersonalStageRequest request);

    PersonalStageDto getById(UUID personalStageId);

    List<PersonalStageDto> getByRoadmapId(UUID roadmapId);

    PersonalStageDto updateStage(UUID personalStageId, UpdatePersonalStageRequest request);

    void deleteStage(UUID personalStageId);
}

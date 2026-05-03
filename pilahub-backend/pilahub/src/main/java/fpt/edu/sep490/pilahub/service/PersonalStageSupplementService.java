package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PersonalStageSupplementDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalStageSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalStageSupplementRequest;

import java.util.List;
import java.util.UUID;

public interface PersonalStageSupplementService {

    PersonalStageSupplementDto createPersonalStageSupplement(CreatePersonalStageSupplementRequest request);

    PersonalStageSupplementDto getById(UUID personalStageSupplementId);

    List<PersonalStageSupplementDto> getByPersonalStageId(UUID personalStageId);

    List<PersonalStageSupplementDto> getByRoadmapId(UUID roadmapId);

    PersonalStageSupplementDto updatePersonalStageSupplement(UUID personalStageSupplementId, UpdatePersonalStageSupplementRequest request);

    void deletePersonalStageSupplement(UUID personalStageSupplementId);

    void deleteByPersonalStageId(UUID personalStageId);
}

package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.AssessmentCriterionDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.CreateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.dto.request.assessment.UpdateAssessmentCriterionRequest;

import java.util.List;
import java.util.UUID;

public interface AssessmentCriterionService {

    AssessmentCriterionDto create(CreateAssessmentCriterionRequest request);

    AssessmentCriterionDto getById(UUID criterionId);

    List<AssessmentCriterionDto> getAll();

    List<AssessmentCriterionDto> getAllActive();

    List<AssessmentCriterionDto> searchByName(String name);

    AssessmentCriterionDto update(UUID criterionId, UpdateAssessmentCriterionRequest request);

    void deactivate(UUID criterionId);
}


package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.AssessmentCriterionDto;
import fpt.edu.sep490.pilahub.dto.request.assessment.CreateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.dto.request.assessment.UpdateAssessmentCriterionRequest;
import fpt.edu.sep490.pilahub.pojo.AssessmentCriterion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AssessmentCriterionMapper {

    AssessmentCriterionDto toDto(AssessmentCriterion criterion);

    @Mapping(target = "assessmentCriterionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    AssessmentCriterion toEntity(CreateAssessmentCriterionRequest request);

    @Mapping(target = "assessmentCriterionId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget AssessmentCriterion criterion, UpdateAssessmentCriterionRequest request);
}


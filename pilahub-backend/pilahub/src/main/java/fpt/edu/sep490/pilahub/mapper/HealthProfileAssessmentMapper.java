package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.pojo.HealthProfileAssessment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthProfileAssessmentMapper {

    @Mapping(source = "healthProfile.healthProfileId", target = "healthProfileId")
    HealthProfileAssessmentDto toDto(HealthProfileAssessment assessment);
}

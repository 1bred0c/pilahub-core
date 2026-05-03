package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.AssessmentResultDto;
import fpt.edu.sep490.pilahub.dto.SessionAssessmentDto;
import fpt.edu.sep490.pilahub.pojo.AssessmentResult;
import fpt.edu.sep490.pilahub.pojo.SessionAssessment;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class SessionAssessmentMapper {

    public SessionAssessmentDto toDto(SessionAssessment sessionAssessment) {
        if (sessionAssessment == null) {
            return null;
        }

        List<AssessmentResultDto> resultDtos = sessionAssessment.getResults().stream()
                .sorted(Comparator.comparing(result -> result.getCriterion().getDisplayOrder()))
                .map(this::toResultDto)
                .toList();

        return new SessionAssessmentDto(
                sessionAssessment.getLiveSessionId(),
                sessionAssessment.getCoachId(),
                sessionAssessment.getTraineeId(),
                sessionAssessment.getSubmittedAt(),
                resultDtos
        );
    }

    private AssessmentResultDto toResultDto(AssessmentResult result) {
        return new AssessmentResultDto(
                result.getCriterion().getAssessmentCriterionId(),
                result.getCriterion().getName(),
                result.getCriterion().getDisplayOrder(),
                result.getScore()
        );
    }
}


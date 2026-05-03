package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CoachFeedbackDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachFeedbackRequest;
import fpt.edu.sep490.pilahub.pojo.CoachFeedback;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoachFeedbackMapper {

    @Mapping(source = "coach.coachId", target = "coachId")
    @Mapping(source = "coach.fullName", target = "coachFullName")
    @Mapping(source = "trainee.traineeId", target = "traineeId")
    @Mapping(source = "trainee.fullName", target = "traineeFullName")
    @Mapping(source = "trainee.avatarUrl", target = "traineeAvatarUrl")
    CoachFeedbackDto toDto(CoachFeedback coachFeedback);

    @Mapping(target = "feedbackId", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CoachFeedback toEntity(CreateCoachFeedbackRequest request);

    @Mapping(target = "feedbackId", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCoachFeedbackRequest request, @MappingTarget CoachFeedback coachFeedback);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CoachRoadmapRequestDto;
import fpt.edu.sep490.pilahub.dto.TrainingDayScheduleDto;
import fpt.edu.sep490.pilahub.pojo.CoachRoadmapRequest;
import fpt.edu.sep490.pilahub.pojo.TrainingDaySchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoachRoadmapRequestMapper {

    @Mapping(target = "traineeId", source = "trainee.traineeId")
    @Mapping(target = "traineeFullName", source = "trainee.fullName")
    @Mapping(target = "traineeAvatarUrl", source = "trainee.avatarUrl")
    @Mapping(target = "coachId", source = "coach.coachId")
    @Mapping(target = "coachFullName", source = "coach.fullName")
    @Mapping(target = "primaryGoalId", source = "primaryGoal.goalId")
    @Mapping(target = "primaryGoalName", source = "primaryGoal.vietnameseName")
    @Mapping(target = "secondaryGoalNames", ignore = true)
    CoachRoadmapRequestDto toDto(CoachRoadmapRequest entity);

    TrainingDayScheduleDto toDto(TrainingDaySchedule schedule);
}

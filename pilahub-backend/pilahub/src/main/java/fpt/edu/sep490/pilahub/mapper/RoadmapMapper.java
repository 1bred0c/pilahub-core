package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.RoadmapDto;
import fpt.edu.sep490.pilahub.dto.RoadmapGoalDto;
import fpt.edu.sep490.pilahub.pojo.Roadmap;
import fpt.edu.sep490.pilahub.pojo.RoadmapGoal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface RoadmapMapper {

    @Mapping(target = "status", expression = "java(roadmap.getStatus().toString())")
    @Mapping(target = "traineeId", expression = "java(roadmap.getTrainee() != null ? roadmap.getTrainee().getTraineeId() : null)")
    @Mapping(target = "coachId", expression = "java(roadmap.getCoach() != null ? roadmap.getCoach().getCoachId() : null)")
    @Mapping(target = "goals", expression = "java(mapRoadmapGoals(roadmap.getRoadmapGoals()))")
    @Mapping(target = "totalAmount", ignore = true)
    RoadmapDto toDto(Roadmap roadmap);

    default List<RoadmapGoalDto> mapRoadmapGoals(List<RoadmapGoal> roadmapGoals) {
        if (roadmapGoals == null) {
            return null;
        }
        return roadmapGoals.stream()
                .map(rg -> new RoadmapGoalDto(
                        rg.getRoadmapGoalId(),
                        rg.getFitnessGoal() != null ? rg.getFitnessGoal().getGoalId() : null,
                        rg.getFitnessGoal() != null ? rg.getFitnessGoal().getCode() : null,
                        rg.getFitnessGoal() != null ? rg.getFitnessGoal().getVietnameseName() : null,
                        rg.getIsPrimary(),
                        rg.getGoalOrder()
                ))
                .collect(Collectors.toList());
    }
}

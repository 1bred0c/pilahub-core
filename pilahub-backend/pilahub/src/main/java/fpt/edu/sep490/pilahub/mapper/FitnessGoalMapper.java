package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.FitnessGoalDto;
import fpt.edu.sep490.pilahub.pojo.FitnessGoal;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface FitnessGoalMapper {

    FitnessGoalDto toDto(FitnessGoal fitnessGoal);
}

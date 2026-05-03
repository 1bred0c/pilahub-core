package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TraineeDto;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TraineeMapper {

    TraineeDto toDto(Trainee trainee);
}

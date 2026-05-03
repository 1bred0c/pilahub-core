package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CoachTimeOffDto;
import fpt.edu.sep490.pilahub.pojo.CoachTimeOff;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {CoachMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoachTimeOffMapper {

    CoachTimeOffDto toDto(CoachTimeOff coachTimeOff);
}


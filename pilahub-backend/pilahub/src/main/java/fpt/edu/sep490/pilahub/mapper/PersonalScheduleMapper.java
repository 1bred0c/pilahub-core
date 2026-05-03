package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.pojo.PersonalSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonalScheduleMapper {

    @Mapping(target = "personalStageId", source = "personalStage.personalStageId")
    PersonalScheduleDto toDto(PersonalSchedule schedule);
}

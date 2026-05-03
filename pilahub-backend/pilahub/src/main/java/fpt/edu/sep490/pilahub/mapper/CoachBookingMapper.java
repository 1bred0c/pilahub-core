package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = { CoachMapper.class,
        TraineeMapper.class }, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoachBookingMapper {

    @Mapping(target = "personalSchedule", ignore = true)
    CoachBookingDto toDto(CoachBooking coachBooking);
}

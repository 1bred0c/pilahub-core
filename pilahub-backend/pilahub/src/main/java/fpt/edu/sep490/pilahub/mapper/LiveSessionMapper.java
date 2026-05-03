package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.LiveSessionDto;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {CoachBookingMapper.class})
public interface LiveSessionMapper {

    @Mapping(target = "coachBooking", source = "coachBooking")
    LiveSessionDto toDto(LiveSession liveSession);

    @Mapping(target = "coachBooking", ignore = true)
    LiveSession toEntity(LiveSessionDto liveSessionDto);
}


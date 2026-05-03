package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.HeartRateLogDto;
import fpt.edu.sep490.pilahub.pojo.HeartRateLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HeartRateLogMapper {

    @Mapping(target = "workoutSessionId", source = "workoutSession.workoutSessionId")
    HeartRateLogDto toDto(HeartRateLog heartRateLog);
}


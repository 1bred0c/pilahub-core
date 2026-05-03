package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CoachDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachRequest;
import fpt.edu.sep490.pilahub.pojo.Coach;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CoachMapper {

    CoachDto toDto(Coach coach);

    @Mapping(target = "coachId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "pricePerHour", ignore = true)
    Coach toEntity(CreateCoachRequest request);

    @Mapping(target = "coachId", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "pricePerHour", ignore = true)
    void updateEntityFromRequest(UpdateCoachRequest request, @MappingTarget Coach coach);
}

package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import fpt.edu.sep490.pilahub.dto.request.CreateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateHealthProfileRequest;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface HealthProfileMapper {

    @Mapping(target = "traineeId", source = "trainee.traineeId")
    HealthProfileDto toDto(HealthProfile healthProfile);

    @Mapping(target = "healthProfileId", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    HealthProfile toEntity(CreateHealthProfileRequest request);

    @Mapping(target = "healthProfileId", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UpdateHealthProfileRequest request, @MappingTarget HealthProfile healthProfile);
}

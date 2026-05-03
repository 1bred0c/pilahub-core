package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PersonalInjuryDto;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PersonalInjuryMapper {

    @Mapping(target = "traineeId", source = "trainee.traineeId")
    @Mapping(target = "injuryId", source = "injury.injuryId")
    @Mapping(target = "injuryName", source = "injury.name")
    @Mapping(target = "injuryDescription", source = "injury.description")
    PersonalInjuryDto toDto(PersonalInjury personalInjury);
}

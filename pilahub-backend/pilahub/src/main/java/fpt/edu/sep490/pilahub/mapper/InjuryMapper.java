package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.InjuryDto;
import fpt.edu.sep490.pilahub.pojo.Injury;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {BodyPartMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface InjuryMapper {

    InjuryDto toDto(Injury injury);
}

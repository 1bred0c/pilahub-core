package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TutorialDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateTutorialRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateTutorialRequest;
import fpt.edu.sep490.pilahub.pojo.Tutorial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TutorialMapper {

    @Mapping(target = "exerciseId", source = "exercise.exerciseId")
    TutorialDto toDto(Tutorial tutorial);

    @Mapping(target = "tutorialId", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Tutorial toEntity(CreateTutorialRequest request);

    @Mapping(target = "tutorialId", ignore = true)
    @Mapping(target = "exercise", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateTutorialRequest request, @MappingTarget Tutorial tutorial);
}

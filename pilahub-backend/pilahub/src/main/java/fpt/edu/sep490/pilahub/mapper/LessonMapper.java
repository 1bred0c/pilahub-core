package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.LessonDto;
import fpt.edu.sep490.pilahub.dto.request.lesson.CreateLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.lesson.UpdateLessonRequest;
import fpt.edu.sep490.pilahub.pojo.Lesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface LessonMapper {

    LessonDto toDto(Lesson lesson);

    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    Lesson toEntity(CreateLessonRequest request);

    @Mapping(target = "lessonId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntityFromRequest(UpdateLessonRequest request, @MappingTarget Lesson lesson);
}

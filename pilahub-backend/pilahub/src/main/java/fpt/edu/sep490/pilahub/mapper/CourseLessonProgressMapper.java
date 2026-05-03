package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CourseLessonProgressDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonProgressRequest;
import fpt.edu.sep490.pilahub.pojo.CourseLessonProgress;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {TraineeCourseMapper.class, CourseLessonMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseLessonProgressMapper {

    CourseLessonProgressDto toDto(CourseLessonProgress courseLessonProgress);

    @Mapping(target = "progressId", ignore = true)
    @Mapping(target = "traineeCourse", ignore = true)
    @Mapping(target = "courseLesson", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    @Mapping(target = "completed", ignore = true)
    CourseLessonProgress toEntity(CreateCourseLessonProgressRequest request);

    @Mapping(target = "progressId", ignore = true)
    @Mapping(target = "traineeCourse", ignore = true)
    @Mapping(target = "courseLesson", ignore = true)
    @Mapping(target = "startedAt", ignore = true)
    @Mapping(target = "completedAt", ignore = true)
    void updateEntityFromRequest(UpdateCourseLessonProgressRequest request, @MappingTarget CourseLessonProgress courseLessonProgress);
}

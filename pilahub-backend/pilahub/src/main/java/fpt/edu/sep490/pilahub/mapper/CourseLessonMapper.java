package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CourseLessonDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseLessonRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseLessonRequest;
import fpt.edu.sep490.pilahub.pojo.CourseLesson;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseLessonMapper {

    @Mapping(target = "courseId", source = "course.courseId")
    @Mapping(target = "courseName", source = "course.name")
    @Mapping(target = "lessonId", source = "lesson.lessonId")
    @Mapping(target = "lessonName", source = "lesson.name")
    CourseLessonDto toDto(CourseLesson courseLesson);

    @Mapping(target = "courseLessonId", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    CourseLesson toEntity(CreateCourseLessonRequest request);

    @Mapping(target = "courseLessonId", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "lesson", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromRequest(UpdateCourseLessonRequest request, @MappingTarget CourseLesson courseLesson);
}

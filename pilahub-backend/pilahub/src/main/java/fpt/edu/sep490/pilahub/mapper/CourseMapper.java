package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.CourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateCourseRequest;
import fpt.edu.sep490.pilahub.dto.request.course.UpdateCourseRequest;
import fpt.edu.sep490.pilahub.pojo.Course;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourseMapper {

    @Mapping(target = "totalLesson", expression = "java(course.getCourseLessons() != null ? course.getCourseLessons().size() : 0)")
    CourseDto toDto(Course course);

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "courseLessons", ignore = true)
    Course toEntity(CreateCourseRequest request);

    @Mapping(target = "courseId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "courseLessons", ignore = true)
    void updateEntityFromRequest(UpdateCourseRequest request, @MappingTarget Course course);
}

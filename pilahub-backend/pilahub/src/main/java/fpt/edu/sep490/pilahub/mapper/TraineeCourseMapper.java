package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TraineeCourseDto;
import fpt.edu.sep490.pilahub.dto.request.course.CreateTraineeCourseRequest;
import fpt.edu.sep490.pilahub.pojo.TraineeCourse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {TraineeMapper.class, CourseMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TraineeCourseMapper {

    TraineeCourseDto toDto(TraineeCourse traineeCourse);

    @Mapping(target = "traineeCourseId", ignore = true)
    @Mapping(target = "trainee", ignore = true)
    @Mapping(target = "course", ignore = true)
    @Mapping(target = "enrolledAt", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    TraineeCourse toEntity(CreateTraineeCourseRequest request);
}

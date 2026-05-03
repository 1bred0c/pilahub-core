package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PostDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostRequest;
import fpt.edu.sep490.pilahub.pojo.Post;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMapper {

    @Mapping(target = "coachId", source = "coach.coachId")
    @Mapping(target = "coachName", source = "coach.fullName")
    @Mapping(target = "medias", ignore = true)
    PostDto toDto(Post post);

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Post toEntity(CreatePostRequest request);

    @Mapping(target = "postId", ignore = true)
    @Mapping(target = "coach", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UpdatePostRequest request, @MappingTarget Post post);
}


package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PostMediaDto;
import fpt.edu.sep490.pilahub.dto.request.post.PostMediaUpsertRequest;
import fpt.edu.sep490.pilahub.pojo.PostMedia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostMediaMapper {

    PostMediaDto toDto(PostMedia postMedia);

    @Mapping(target = "postMediaId", ignore = true)
    @Mapping(target = "post", ignore = true)
    PostMedia toEntity(PostMediaUpsertRequest request);
}


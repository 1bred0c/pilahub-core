package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PostReactionDto;
import fpt.edu.sep490.pilahub.pojo.PostReaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PostReactionMapper {

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountName", ignore = true)
    PostReactionDto toDto(PostReaction postReaction);
}


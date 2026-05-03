package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.PostCommentDto;
import fpt.edu.sep490.pilahub.dto.PostCommentReplyDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostCommentRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostCommentRequest;
import fpt.edu.sep490.pilahub.pojo.PostComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PostCommentMapper {

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountName", ignore = true)
    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "hasMoreReplies", ignore = true)
    PostCommentDto toDto(PostComment postComment);

    @Mapping(target = "postId", source = "post.postId")
    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "accountName", ignore = true)
    @Mapping(target = "parentCommentId", source = "parentComment.commentId")
    PostCommentReplyDto toReplyDto(PostComment postComment);

    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    PostComment toEntity(CreatePostCommentRequest request);

    @Mapping(target = "commentId", ignore = true)
    @Mapping(target = "post", ignore = true)
    @Mapping(target = "account", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromRequest(UpdatePostCommentRequest request, @MappingTarget PostComment postComment);
}


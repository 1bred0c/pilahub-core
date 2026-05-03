package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PostCommentDto;
import fpt.edu.sep490.pilahub.dto.PostCommentReplyDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostCommentRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostCommentRequest;

import java.util.List;
import java.util.UUID;

public interface PostCommentService {

    PostCommentDto createComment(UUID accountId, UUID postId, CreatePostCommentRequest request);

    PostCommentDto getById(UUID commentId);

    List<PostCommentDto> getPostLatestRootComments(UUID postId, int rootLimit);

    List<PostCommentDto> getAllPostRootComments(UUID postId);

    List<PostCommentReplyDto> getAllRepliesOfRootComment(UUID rootCommentId);

    List<PostCommentDto> getMyComments(UUID accountId);

    PostCommentDto updateComment(UUID accountId, UUID commentId, UpdatePostCommentRequest request);

    void deleteComment(UUID accountId, UUID commentId);
}


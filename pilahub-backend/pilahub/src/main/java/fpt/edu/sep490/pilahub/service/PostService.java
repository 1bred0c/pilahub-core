package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PostDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostRequest;

import java.util.List;
import java.util.UUID;

public interface PostService {

    PostDto createPost(UUID coachId, CreatePostRequest request);

    PostDto getById(UUID postId);

    List<PostDto> getByCoachId(UUID coachId);

    List<PostDto> getMine(UUID coachId);

    PostDto updatePost(UUID postId, UUID coachId, UpdatePostRequest request);

    void deletePost(UUID postId, UUID coachId);
}


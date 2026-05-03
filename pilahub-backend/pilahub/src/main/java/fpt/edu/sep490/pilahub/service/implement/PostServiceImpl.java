package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PostDto;
import fpt.edu.sep490.pilahub.dto.PostMediaDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostRequest;
import fpt.edu.sep490.pilahub.dto.request.post.PostMediaUpsertRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PostMapper;
import fpt.edu.sep490.pilahub.mapper.PostMediaMapper;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.Post;
import fpt.edu.sep490.pilahub.pojo.PostMedia;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.PostMediaRepository;
import fpt.edu.sep490.pilahub.repository.PostRepository;
import fpt.edu.sep490.pilahub.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final PostMediaRepository postMediaRepository;
    private final CoachRepository coachRepository;
    private final PostMapper postMapper;
    private final PostMediaMapper postMediaMapper;

    @Override
    public PostDto createPost(UUID coachId, CreatePostRequest request) {
        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        validateMediaOrders(request.medias());

        Post post = postMapper.toEntity(request);
        post.setCoach(coach);
        Post savedPost = postRepository.save(post);

        savePostMedia(savedPost, request.medias());

        return buildPostDto(savedPost);
    }

    @Override
    public PostDto getById(UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));
        return buildPostDto(post);
    }

    @Override
    public List<PostDto> getByCoachId(UUID coachId) {
        return postRepository.findByCoach_CoachIdOrderByCreatedAtDesc(coachId).stream()
                .map(this::buildPostDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDto> getMine(UUID coachId) {
        return getByCoachId(coachId);
    }

    @Override
    public PostDto updatePost(UUID postId, UUID coachId, UpdatePostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getCoach().getCoachId().equals(coachId)) {
            throw new IllegalStateException("You can only update your own post");
        }

        validateMediaOrders(request.medias());

        postMapper.updateEntityFromRequest(request, post);
        Post updatedPost = postRepository.save(post);

        postMediaRepository.deleteByPost_PostId(postId);
        savePostMedia(updatedPost, request.medias());

        return buildPostDto(updatedPost);
    }

    @Override
    public void deletePost(UUID postId, UUID coachId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        if (!post.getCoach().getCoachId().equals(coachId)) {
            throw new IllegalStateException("You can only delete your own post");
        }

        postMediaRepository.deleteByPost_PostId(postId);
        postRepository.delete(post);

        log.info("Post {} deleted by coach {}", postId, coachId);
    }

    private void savePostMedia(Post post, List<PostMediaUpsertRequest> mediaRequests) {
        if (mediaRequests == null || mediaRequests.isEmpty()) {
            return;
        }

        List<PostMedia> medias = mediaRequests.stream()
                .map(postMediaMapper::toEntity)
                .peek(media -> media.setPost(post))
                .collect(Collectors.toList());

        postMediaRepository.saveAll(medias);
    }

    private PostDto buildPostDto(Post post) {
        List<PostMediaDto> medias = postMediaRepository.findByPost_PostIdOrderBySortOrderAsc(post.getPostId()).stream()
                .map(postMediaMapper::toDto)
                .collect(Collectors.toList());

        PostDto base = postMapper.toDto(post);
        return new PostDto(
                base.postId(),
                base.coachId(),
                base.coachName(),
                base.content(),
                base.createdAt(),
                medias
        );
    }

    private void validateMediaOrders(List<PostMediaUpsertRequest> mediaRequests) {
        if (mediaRequests == null || mediaRequests.isEmpty()) {
            return;
        }

        Set<Integer> seenOrders = new HashSet<>();
        for (PostMediaUpsertRequest mediaRequest : mediaRequests) {
            Integer order = mediaRequest.sortOrder();
            if (!seenOrders.add(order)) {
                throw new IllegalArgumentException("Media sortOrder must be unique in one post");
            }
        }
    }
}



package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PostCommentDto;
import fpt.edu.sep490.pilahub.dto.PostCommentReplyDto;
import fpt.edu.sep490.pilahub.dto.request.post.CreatePostCommentRequest;
import fpt.edu.sep490.pilahub.dto.request.post.UpdatePostCommentRequest;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PostCommentMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.Post;
import fpt.edu.sep490.pilahub.pojo.PostComment;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.PostCommentRepository;
import fpt.edu.sep490.pilahub.repository.PostRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.PostCommentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PostCommentServiceImpl implements PostCommentService {

    private static final int DEFAULT_ROOT_LIMIT = 5;
    private static final int REPLY_PREVIEW_LIMIT = 3;

    private final PostCommentRepository postCommentRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final CoachRepository coachRepository;
    private final TraineeRepository traineeRepository;
    private final PostCommentMapper postCommentMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PostCommentDto createComment(UUID accountId, UUID postId, CreatePostCommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        PostComment parentRootComment = resolveRootParentComment(postId, request.parentCommentId());

        PostComment postComment = postCommentMapper.toEntity(request);
        postComment.setPost(post);
        postComment.setAccount(account);
        postComment.setParentComment(parentRootComment);

        PostComment saved = postCommentRepository.save(postComment);

        String actorName = resolveAccountName(account);
        publishPostCommentNotification(post, accountId, actorName, saved.getCommentId());
        if (parentRootComment != null) {
            publishReplyNotifications(saved, parentRootComment, actorName);
        }

        return toCommentDto(saved);
    }

    @Override
    public PostCommentDto getById(UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", commentId));
        return toCommentDto(comment);
    }

    @Override
    public List<PostCommentDto> getPostLatestRootComments(UUID postId, int rootLimit) {
        ensurePostExists(postId);
        int validLimit = rootLimit > 0 ? rootLimit : DEFAULT_ROOT_LIMIT;

        return postCommentRepository.findByPost_PostIdAndParentCommentIsNullOrderByCreatedAtDesc(
                postId,
                PageRequest.of(0, validLimit)).stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCommentDto> getAllPostRootComments(UUID postId) {
        ensurePostExists(postId);
        return postCommentRepository.findByPost_PostIdAndParentCommentIsNullOrderByCreatedAtDesc(postId).stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCommentReplyDto> getAllRepliesOfRootComment(UUID rootCommentId) {
        PostComment rootComment = postCommentRepository.findById(rootCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", rootCommentId));

        if (rootComment.getParentComment() != null) {
            throw new IllegalArgumentException("This API only accepts root comment id");
        }

        return postCommentRepository.findByParentComment_CommentIdOrderByCreatedAtAsc(rootCommentId).stream()
                .map(this::toReplyDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostCommentDto> getMyComments(UUID accountId) {
        return postCommentRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostCommentDto updateComment(UUID accountId, UUID commentId, UpdatePostCommentRequest request) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", commentId));

        if (!comment.getAccount().getAccountId().equals(accountId)) {
            throw new IllegalStateException("You can only update your own comment");
        }

        postCommentMapper.updateEntityFromRequest(request, comment);
        PostComment updated = postCommentRepository.save(comment);
        return toCommentDto(updated);
    }

    @Override
    public void deleteComment(UUID accountId, UUID commentId) {
        PostComment comment = postCommentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", commentId));

        if (!comment.getAccount().getAccountId().equals(accountId)) {
            throw new IllegalStateException("You can only delete your own comment");
        }

        if (comment.getParentComment() == null) {
            List<PostComment> replies = postCommentRepository
                    .findByParentComment_CommentIdOrderByCreatedAtAsc(commentId);
            if (!replies.isEmpty()) {
                postCommentRepository.deleteAll(replies);
            }
        }

        postCommentRepository.delete(comment);
        log.info("Comment {} deleted by account {}", commentId, accountId);
    }

    private PostComment resolveRootParentComment(UUID postId, UUID parentCommentId) {
        if (parentCommentId == null) {
            return null;
        }

        PostComment parent = postCommentRepository.findById(parentCommentId)
                .orElseThrow(() -> new ResourceNotFoundException("PostComment", "id", parentCommentId));

        if (!parent.getPost().getPostId().equals(postId)) {
            throw new IllegalArgumentException("Parent comment does not belong to the target post");
        }

        // Max depth = 1: reply-to-reply is flattened to the root parent comment.
        return parent.getParentComment() == null ? parent : parent.getParentComment();
    }

    private PostCommentDto toCommentDto(PostComment comment) {
        PostCommentDto base = postCommentMapper.toDto(comment);

        List<PostCommentReplyDto> replies = new ArrayList<>();
        boolean hasMoreReplies = false;
        if (comment.getParentComment() == null) {
            List<PostComment> previewReplies = postCommentRepository
                    .findTop3ByParentComment_CommentIdOrderByCreatedAtAsc(comment.getCommentId());
            replies = previewReplies.stream().map(this::toReplyDto).collect(Collectors.toList());
            hasMoreReplies = postCommentRepository
                    .countByParentComment_CommentId(comment.getCommentId()) > REPLY_PREVIEW_LIMIT;
        }

        return new PostCommentDto(
                base.commentId(),
                base.postId(),
                base.accountId(),
                resolveAccountName(comment.getAccount()),
                base.content(),
                base.parentCommentId(),
                base.createdAt(),
                replies,
                hasMoreReplies);
    }

    private PostCommentReplyDto toReplyDto(PostComment reply) {
        PostCommentReplyDto base = postCommentMapper.toReplyDto(reply);
        return new PostCommentReplyDto(
                base.commentId(),
                base.postId(),
                base.accountId(),
                resolveAccountName(reply.getAccount()),
                base.content(),
                base.parentCommentId(),
                base.createdAt());
    }

    private void publishPostCommentNotification(Post post, UUID actorId, String actorName, UUID commentId) {
        UUID coachId = post.getCoach().getCoachId();
        if (!coachId.equals(actorId)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    coachId,
                    NotificationType.POST_COMMENTED,
                    "Bình Luận Mới Trên Bài Viết Của Bạn",
                    actorName + " đã bình luận trên bài viết của bạn.",
                    commentId,
                    "POST_COMMENT"));
        }
    }

    private void publishReplyNotifications(PostComment savedComment, PostComment rootComment, String actorName) {
        UUID actorId = savedComment.getAccount().getAccountId();
        UUID coachId = savedComment.getPost().getCoach().getCoachId();

        Set<UUID> participantIds = new LinkedHashSet<>();
        participantIds.add(rootComment.getAccount().getAccountId());
        participantIds.addAll(
                postCommentRepository.findByParentComment_CommentIdOrderByCreatedAtAsc(rootComment.getCommentId())
                        .stream()
                        .map(reply -> reply.getAccount().getAccountId())
                        .collect(Collectors.toSet()));

        participantIds.remove(actorId);
        participantIds.remove(coachId);

        for (UUID participantId : participantIds) {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    participantId,
                    NotificationType.POST_COMMENT_REPLIED,
                    "Phản Hồi Mới Trong Chủ Đề Bình Luận",
                    actorName + " đã phản hồi trong chủ đề bình luận mà bạn tham gia.",
                    savedComment.getCommentId(),
                    "POST_COMMENT"));
        }
    }

    private String resolveAccountName(Account account) {
        if (account == null) {
            return "Unknown";
        }

        UUID accountId = account.getAccountId();
        Role role = account.getRole();
        if (role == Role.COACH) {
            return coachRepository.findById(accountId)
                    .map(Coach::getFullName)
                    .orElse(account.getEmail());
        }
        if (role == Role.TRAINEE) {
            return traineeRepository.findById(accountId)
                    .map(Trainee::getFullName)
                    .orElse(account.getEmail());
        }
        return account.getEmail();
    }

    private void ensurePostExists(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
    }
}

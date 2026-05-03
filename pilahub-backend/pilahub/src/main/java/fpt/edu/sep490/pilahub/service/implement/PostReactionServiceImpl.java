package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PostReactionDto;
import fpt.edu.sep490.pilahub.dto.PostReactionSummaryDto;
import fpt.edu.sep490.pilahub.enums.NotificationType;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.event.NotificationEvent;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PostReactionMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.Post;
import fpt.edu.sep490.pilahub.pojo.PostReaction;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.repository.PostReactionRepository;
import fpt.edu.sep490.pilahub.repository.PostRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.PostReactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostReactionServiceImpl implements PostReactionService {

    private final PostReactionRepository postReactionRepository;
    private final PostRepository postRepository;
    private final AccountRepository accountRepository;
    private final CoachRepository coachRepository;
    private final TraineeRepository traineeRepository;
    private final PostReactionMapper postReactionMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public PostReactionDto react(UUID accountId, UUID postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        PostReaction reaction = postReactionRepository
                .findByAccount_AccountIdAndPost_PostId(accountId, postId)
                .orElseGet(() -> postReactionRepository.save(PostReaction.builder()
                        .account(account)
                        .post(post)
                        .build()));

        if (!post.getCoach().getCoachId().equals(accountId)) {
            eventPublisher.publishEvent(new NotificationEvent(
                    this,
                    post.getCoach().getCoachId(),
                    NotificationType.POST_REACTED,
                    "Phản Ứng Mới Trên Bài Viết Của Bạn",
                    resolveAccountName(account) + " đã phản ứng với bài viết của bạn.",
                    postId,
                    "POST"));
        }

        return toReactionDto(reaction);
    }

    @Override
    public void unreact(UUID accountId, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }

        postReactionRepository.deleteByAccount_AccountIdAndPost_PostId(accountId, postId);
    }

    @Override
    public PostReactionSummaryDto toggleReaction(UUID accountId, UUID postId) {
        if (postReactionRepository.existsByAccount_AccountIdAndPost_PostId(accountId, postId)) {
            unreact(accountId, postId);
            return new PostReactionSummaryDto(postId, postReactionRepository.countByPost_PostId(postId), false);
        }

        react(accountId, postId);
        return new PostReactionSummaryDto(postId, postReactionRepository.countByPost_PostId(postId), true);
    }

    @Override
    public List<PostReactionDto> getPostReactions(UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }

        return postReactionRepository.findByPost_PostIdOrderByCreatedAtDesc(postId).stream()
                .map(this::toReactionDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostReactionDto> getMyReactions(UUID accountId) {
        return postReactionRepository.findByAccount_AccountIdOrderByCreatedAtDesc(accountId).stream()
                .map(this::toReactionDto)
                .collect(Collectors.toList());
    }

    @Override
    public PostReactionSummaryDto getReactionSummary(UUID accountId, UUID postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }

        long reactionCount = postReactionRepository.countByPost_PostId(postId);
        boolean reactedByMe = postReactionRepository.existsByAccount_AccountIdAndPost_PostId(accountId, postId);
        return new PostReactionSummaryDto(postId, reactionCount, reactedByMe);
    }

    private PostReactionDto toReactionDto(PostReaction reaction) {
        PostReactionDto base = postReactionMapper.toDto(reaction);
        return new PostReactionDto(
                base.postId(),
                base.accountId(),
                resolveAccountName(reaction.getAccount()),
                base.createdAt());
    }

    private String resolveAccountName(Account account) {
        if (account == null) {
            return "Unknown";
        }

        UUID accountId = account.getAccountId();
        if (account.getRole() == Role.COACH) {
            return coachRepository.findById(accountId)
                    .map(Coach::getFullName)
                    .orElse(account.getEmail());
        }
        if (account.getRole() == Role.TRAINEE) {
            return traineeRepository.findById(accountId)
                    .map(Trainee::getFullName)
                    .orElse(account.getEmail());
        }
        return account.getEmail();
    }
}

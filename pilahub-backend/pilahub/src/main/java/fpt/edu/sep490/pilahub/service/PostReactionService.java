package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PostReactionDto;
import fpt.edu.sep490.pilahub.dto.PostReactionSummaryDto;

import java.util.List;
import java.util.UUID;

public interface PostReactionService {

    PostReactionDto react(UUID accountId, UUID postId);

    void unreact(UUID accountId, UUID postId);

    PostReactionSummaryDto toggleReaction(UUID accountId, UUID postId);

    List<PostReactionDto> getPostReactions(UUID postId);

    List<PostReactionDto> getMyReactions(UUID accountId);

    PostReactionSummaryDto getReactionSummary(UUID accountId, UUID postId);
}


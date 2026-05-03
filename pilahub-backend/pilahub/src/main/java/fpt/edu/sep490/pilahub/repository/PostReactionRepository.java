package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PostReaction;
import fpt.edu.sep490.pilahub.pojo.PostReactionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, PostReactionId> {

    Optional<PostReaction> findByAccount_AccountIdAndPost_PostId(UUID accountId, UUID postId);

    boolean existsByAccount_AccountIdAndPost_PostId(UUID accountId, UUID postId);

    long countByPost_PostId(UUID postId);

    List<PostReaction> findByPost_PostIdOrderByCreatedAtDesc(UUID postId);

    List<PostReaction> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);

    void deleteByAccount_AccountIdAndPost_PostId(UUID accountId, UUID postId);
}


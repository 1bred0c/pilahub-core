package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PostComment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, UUID> {

    List<PostComment> findByPost_PostIdAndParentCommentIsNullOrderByCreatedAtDesc(UUID postId, Pageable pageable);

    List<PostComment> findByPost_PostIdAndParentCommentIsNullOrderByCreatedAtDesc(UUID postId);

    List<PostComment> findByParentComment_CommentIdOrderByCreatedAtAsc(UUID parentCommentId);

    List<PostComment> findTop3ByParentComment_CommentIdOrderByCreatedAtAsc(UUID parentCommentId);

    long countByParentComment_CommentId(UUID parentCommentId);

    List<PostComment> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);
}


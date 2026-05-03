package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.PostMedia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, UUID> {

    List<PostMedia> findByPost_PostIdOrderBySortOrderAsc(UUID postId);

    void deleteByPost_PostId(UUID postId);
}


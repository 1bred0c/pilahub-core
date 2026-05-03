package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByCoach_CoachIdOrderByCreatedAtDesc(UUID coachId);
}


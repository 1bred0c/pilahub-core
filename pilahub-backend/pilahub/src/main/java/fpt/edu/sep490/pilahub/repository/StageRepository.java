package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Stage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StageRepository extends JpaRepository<Stage, UUID> {
    
    List<Stage> findAllByActiveTrue();
    
    Optional<Stage> findByNameIgnoreCase(String name);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Coach;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CoachRepository extends JpaRepository<Coach, UUID> {

    List<Coach> findByActiveTrue();

    List<Coach> findByFullNameContainingIgnoreCase(String name);
}

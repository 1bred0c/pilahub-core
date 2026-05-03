package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Trainee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TraineeRepository extends JpaRepository<Trainee, UUID> {
}

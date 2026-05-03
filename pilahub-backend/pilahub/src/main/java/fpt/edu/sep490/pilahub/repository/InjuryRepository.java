package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Injury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InjuryRepository extends JpaRepository<Injury, UUID> {

    Optional<Injury> findByName(String name);

    boolean existsByName(String name);
}

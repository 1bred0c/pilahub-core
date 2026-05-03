package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.BodyPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BodyPartRepository extends JpaRepository<BodyPart, UUID> {

    Optional<BodyPart> findByName(String name);

    Optional<BodyPart> findByNameIgnoreCase(String name);

    List<BodyPart> findByNameIn(Collection<String> names);

    List<BodyPart> findByNameContainingIgnoreCase(String name);

    List<BodyPart> findAllByOrderByNameAsc();

    boolean existsByName(String name);

    boolean existsByNameIgnoreCase(String name);
}

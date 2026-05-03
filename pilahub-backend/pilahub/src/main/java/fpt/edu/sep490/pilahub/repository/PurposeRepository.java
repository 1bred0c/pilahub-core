package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurposeRepository extends JpaRepository<Purpose, UUID> {

    List<Purpose> findByActiveTrue();

    Optional<Purpose> findByPurposeIdAndActiveTrue(UUID purposeId);

    Optional<Purpose> findByName(String name);

    Optional<Purpose> findByCode(String code);

    Optional<Purpose> findByCodeAndActiveTrue(String code);

    List<Purpose> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByCode(String code);

    boolean existsByPurposeId(UUID purposeId);
}

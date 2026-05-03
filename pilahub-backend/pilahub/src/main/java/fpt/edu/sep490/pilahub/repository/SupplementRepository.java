package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Supplement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementRepository extends JpaRepository<Supplement, UUID> {

    List<Supplement> findByActiveTrue();

    Optional<Supplement> findBySupplementIdAndActiveTrue(UUID supplementId);

    List<Supplement> findByNameContainingIgnoreCase(String name);

    Optional<Supplement> findByName(String name);

    List<Supplement> findByBrand(String brand);

    List<Supplement> findByBrandAndActiveTrue(String brand);

    boolean existsBySupplementId(UUID supplementId);

    boolean existsByName(String name);
}

package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.SystemConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SystemConfigRepository extends JpaRepository<SystemConfig, UUID> {

    Optional<SystemConfig> findTopByOrderByCreatedAtDesc();

    Optional<SystemConfig> findByKey(String key);

    boolean existsByKey(String key);
}

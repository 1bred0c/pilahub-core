package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportReasonRepository extends JpaRepository<ReportReason, UUID> {

    Optional<ReportReason> findByCode(String code);

    Optional<ReportReason> findByCodeAndActiveTrue(String code);

    List<ReportReason> findByActiveTrue();

    List<ReportReason> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByCode(String code);
}


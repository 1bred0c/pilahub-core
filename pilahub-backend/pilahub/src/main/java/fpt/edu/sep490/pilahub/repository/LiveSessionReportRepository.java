package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.LiveSessionReport;
import fpt.edu.sep490.pilahub.pojo.ReportReason;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LiveSessionReportRepository extends JpaRepository<LiveSessionReport, UUID> {

    /**
     * Find report by live session ID
     */
    Optional<LiveSessionReport> findByLiveSessionId(UUID liveSessionId);

    /**
     * Find all unresolved reports
     */
    List<LiveSessionReport> findByResolvedAtIsNull();

    /**
     * Find all reports by reporter ID
     */
    List<LiveSessionReport> findByReporterId(UUID reporterId);

    /**
     * Find all reports for a specific coach
     */
    List<LiveSessionReport> findByReportedUserId(UUID reportedUserId);

    /**
     * Check if report exists for a session
     */
    boolean existsByLiveSessionId(UUID liveSessionId);

    List<LiveSessionReport> findByReason(ReportReason reason);
}


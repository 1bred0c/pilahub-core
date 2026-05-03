package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.LiveSessionReportDto;
import fpt.edu.sep490.pilahub.dto.request.CreateLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.dto.request.ResolveLiveSessionReportRequest;

import java.util.List;
import java.util.UUID;

public interface LiveSessionReportService {

    /**
     * Create a new report for a completed live session
     * Only trainee can create report for their own session
     * Session must be COMPLETED
     * One session can only have one report
     */
    LiveSessionReportDto createReport(UUID liveSessionId, CreateLiveSessionReportRequest request);

    /**
     * Get report by live session ID
     */
    LiveSessionReportDto getReportByLiveSessionId(UUID liveSessionId);

    /**
     * Get all unresolved reports (for admin)
     */
    List<LiveSessionReportDto> getUnresolvedReports();

    /**
     * Get all reports (for admin)
     */
    List<LiveSessionReportDto> getAllReports();

    /**
     * Get all reports by reporter ID
     */
    List<LiveSessionReportDto> getReportsByReporterId(UUID reporterId);

    /**
     * Get all reports for a specific coach
     */
    List<LiveSessionReportDto> getReportsByCoachId(UUID coachId);

    /**
     * Resolve a report by admin
     * Sets resolved_at, resolved_by, and internal_note
     */
    LiveSessionReportDto resolveReport(UUID liveSessionId, ResolveLiveSessionReportRequest request);

    /**
     * Get all reports created by current trainee
     */
    List<LiveSessionReportDto> getMyCreatedReports();

    /**
     * Get all reports received (filed against) current coach
     */
    List<LiveSessionReportDto> getMyReceivedReports();
}



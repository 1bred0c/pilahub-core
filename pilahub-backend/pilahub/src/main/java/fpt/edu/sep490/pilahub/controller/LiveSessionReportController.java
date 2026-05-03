package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.LiveSessionReportDto;
import fpt.edu.sep490.pilahub.dto.request.CreateLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.dto.request.ResolveLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.LiveSessionReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/live-session-reports")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Live Session Report", description = "Live session report management for trainee complaints and admin resolution")
public class LiveSessionReportController {

    private final LiveSessionReportService reportService;

    @PostMapping("/{liveSessionId}")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Create report for completed session",
            description = "Trainee creates a report for a completed coaching session. " +
                    "Only trainee of the session can report. " +
                    "Session must be COMPLETED. " +
                    "One session can only have one report. " +
                    "If selected reason requires description, description is mandatory."
    )
    @ApiResponse(responseCode = "201", description = "Report created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request - session not completed, report already exists, or description missing")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only trainee can create report")
    @ApiResponse(responseCode = "404", description = "Live session not found")
    @ApiResponse(responseCode = "409", description = "Conflict - Report already exists for this session")
    public ResponseEntity<APIResponse<LiveSessionReportDto>> createReport(
            @PathVariable UUID liveSessionId,
            @Valid @RequestBody CreateLiveSessionReportRequest request) {
        LiveSessionReportDto report = reportService.createReport(liveSessionId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                APIResponse.success("Report created successfully. Notification email sent to both parties.", report)
        );
    }

    @GetMapping("/{liveSessionId}")
    @PreAuthorize("hasAnyRole('TRAINEE', 'COACH', 'ADMIN')")
    @Operation(
            summary = "Get report by live session ID",
            description = "Retrieve report for a specific live session. " +
                    "Trainees can see their own reports. " +
                    "Coaches can see reports filed against them. " +
                    "Admin can see all reports."
    )
    @ApiResponse(responseCode = "200", description = "Report retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<APIResponse<LiveSessionReportDto>> getReportByLiveSessionId(
            @PathVariable UUID liveSessionId) {
        LiveSessionReportDto report = reportService.getReportByLiveSessionId(liveSessionId);
        return ResponseEntity.ok(APIResponse.success("Report retrieved successfully", report));
    }

    @GetMapping("/unresolved")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all unresolved reports",
            description = "Admin only. Retrieve all reports that have not been resolved yet."
    )
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can access this")
    public ResponseEntity<APIResponse<List<LiveSessionReportDto>>> getUnresolvedReports() {
        List<LiveSessionReportDto> reports = reportService.getUnresolvedReports();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d unresolved report(s)", reports.size()),
                reports
        ));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Get all reports",
            description = "Admin only. Retrieve all live session reports."
    )
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can access this")
    public ResponseEntity<APIResponse<List<LiveSessionReportDto>>> getAllReports() {
        List<LiveSessionReportDto> reports = reportService.getAllReports();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d report(s)", reports.size()),
                reports
        ));
    }

    @GetMapping("/my-reports/created")
    @PreAuthorize("hasRole('TRAINEE')")
    @Operation(
            summary = "Get reports I created",
            description = "Trainee: Get all reports they created"
    )
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<LiveSessionReportDto>>> getMyCreatedReports() {
        // Service will get current user ID via SecurityUtil
        List<LiveSessionReportDto> reports = reportService.getMyCreatedReports();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d report(s) created by you", reports.size()),
                reports
        ));
    }

    @GetMapping("/my-reports/received")
    @PreAuthorize("hasRole('COACH')")
    @Operation(
            summary = "Get reports I received",
            description = "Coach: Get all reports filed against them"
    )
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    public ResponseEntity<APIResponse<List<LiveSessionReportDto>>> getMyReceivedReports() {
        // Service will get current user ID via SecurityUtil
        List<LiveSessionReportDto> reports = reportService.getMyReceivedReports();
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d report(s) filed against you", reports.size()),
                reports
        ));
    }

    @GetMapping("/coach/{coachId}")
    @PreAuthorize("hasAnyRole('COACH', 'ADMIN')")
    @Operation(
            summary = "Get reports for a coach",
            description = "Get all reports filed against a specific coach. " +
                    "Coach can only view reports against themselves. Admin can view any coach's reports."
    )
    @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Coach can only view reports against themselves")
    public ResponseEntity<APIResponse<List<LiveSessionReportDto>>> getReportsByCoachId(
            @PathVariable UUID coachId) {
        List<LiveSessionReportDto> reports = reportService.getReportsByCoachId(coachId);
        return ResponseEntity.ok(APIResponse.success(
                String.format("Retrieved %d report(s) for coach", reports.size()),
                reports
        ));
    }

    @PutMapping("/{liveSessionId}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Resolve report",
            description = "Admin only. Resolve a report by adding internal notes and marking as resolved."
    )
    @ApiResponse(responseCode = "200", description = "Report resolved successfully")
    @ApiResponse(responseCode = "400", description = "Report already resolved or invalid request")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing JWT token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Only admin can resolve reports")
    @ApiResponse(responseCode = "404", description = "Report not found")
    public ResponseEntity<APIResponse<LiveSessionReportDto>> resolveReport(
            @PathVariable UUID liveSessionId,
            @Valid @RequestBody ResolveLiveSessionReportRequest request) {
        LiveSessionReportDto report = reportService.resolveReport(liveSessionId, request);
        return ResponseEntity.ok(APIResponse.success("Report resolved successfully", report));
    }
}





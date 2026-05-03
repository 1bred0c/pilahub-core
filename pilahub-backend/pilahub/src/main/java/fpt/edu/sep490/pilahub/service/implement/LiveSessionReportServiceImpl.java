package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.LiveSessionReportDto;
import fpt.edu.sep490.pilahub.dto.request.CreateLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.dto.request.ResolveLiveSessionReportRequest;
import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LiveSessionReportMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.EmailService;
import fpt.edu.sep490.pilahub.service.LiveSessionReportService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LiveSessionReportServiceImpl implements LiveSessionReportService {

    private final LiveSessionReportRepository reportRepository;
    private final LiveSessionRepository liveSessionRepository;
    private final CoachBookingRepository coachBookingRepository;
    private final TraineeRepository traineeRepository;
    private final CoachRepository coachRepository;
    private final AccountRepository accountRepository;
    private final ReportReasonRepository reportReasonRepository;
    private final EmailService emailService;
    private final LiveSessionReportMapper reportMapper;
    private final SecurityUtil securityUtil;

    @Override
    public LiveSessionReportDto createReport(UUID liveSessionId, CreateLiveSessionReportRequest request) {
        log.info("Creating report for live session ID: {}", liveSessionId);

        // Step 1: Get current user (trainee)
        Account currentUser = securityUtil.getCurrentUser();
        UUID traineeUserId = currentUser.getAccountId();

        // Step 2: Find live session
        LiveSession liveSession = liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession", "id", liveSessionId));

        // Step 3: Validate session status is COMPLETED
        if (liveSession.getStatus() != LiveSessionStatus.COMPLETED) {
            throw new InvalidRequestException(
                    String.format("Report can only be created for COMPLETED sessions. Current status: %s", liveSession.getStatus())
            );
        }

        // Step 4: Verify trainee is the reporter (only trainee of session can report)
        CoachBooking booking = liveSession.getCoachBooking();
        if (!booking.getTrainee().getTraineeId().equals(traineeUserId)) {
            throw new AccessDeniedException("Only the trainee of this session can create a report");
        }

        // Step 5: Check if report already exists (one session = one report max)
        if (reportRepository.existsByLiveSessionId(liveSessionId)) {
            throw new InvalidRequestException("A report already exists for this session (HTTP 409 Conflict)");
        }

        // Step 6: Resolve active reason by code
        String reasonCode = request.reason().trim().toUpperCase();
        ReportReason reportReason = reportReasonRepository.findByCodeAndActiveTrue(reasonCode)
                .orElseThrow(() -> new ResourceNotFoundException("ReportReason", "code", reasonCode));

        // Step 7: Validate description requirement configured by admin
        if (reportReason.isRequiresDescription()
                && (request.description() == null || request.description().isBlank())) {
            throw new InvalidRequestException("Description is required for reason: " + reportReason.getCode());
        }

        // Step 8: Create report directly (avoid mapper due to @MapsId relationship)
        LiveSessionReport report = new LiveSessionReport();
        report.setLiveSession(liveSession);
        report.setReporterId(traineeUserId);
        report.setReportedUserId(booking.getCoach().getCoachId());
        report.setReason(reportReason);
        report.setDescription(request.description());
        report.setCreatedAt(Instant.now());

        LiveSessionReport saved = reportRepository.save(report);
        log.info("Report created successfully for live session: {}", liveSessionId);

        // Step 9: Send email notifications to both trainee and coach
        sendReportNotificationEmails(saved, booking);

        return reportMapper.toDto(saved);
    }

    @Override
    public LiveSessionReportDto getReportByLiveSessionId(UUID liveSessionId) {
        log.info("Fetching report for live session ID: {}", liveSessionId);

        LiveSessionReport report = reportRepository.findByLiveSessionId(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "liveSessionId", liveSessionId));

        return reportMapper.toDto(report);
    }

    @Override
    public List<LiveSessionReportDto> getUnresolvedReports() {
        log.info("Fetching all unresolved reports");

        return reportRepository.findByResolvedAtIsNull().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveSessionReportDto> getAllReports() {
        log.info("Fetching all live session reports");

        return reportRepository.findAll().stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveSessionReportDto> getReportsByReporterId(UUID reporterId) {
        log.info("Fetching all reports by reporter ID: {}", reporterId);

        return reportRepository.findByReporterId(reporterId).stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveSessionReportDto> getReportsByCoachId(UUID coachId) {
        log.info("Fetching all reports for coach ID: {}", coachId);

        return reportRepository.findByReportedUserId(coachId).stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LiveSessionReportDto resolveReport(UUID liveSessionId, ResolveLiveSessionReportRequest request) {
        log.info("Resolving report for live session ID: {}", liveSessionId);

        // Get current admin user
        Account adminUser = securityUtil.getCurrentUser();
        UUID adminId = adminUser.getAccountId();

        // Find report
        LiveSessionReport report = reportRepository.findByLiveSessionId(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Report", "liveSessionId", liveSessionId));

        // Check if already resolved
        if (report.getResolvedAt() != null) {
            throw new InvalidRequestException("Report is already resolved");
        }

        // Update resolution fields
        report.setResolvedAt(Instant.now());
        report.setResolvedBy(adminId);
        report.setInternalNote(request.internalNote());

        LiveSessionReport saved = reportRepository.save(report);
        log.info("Report resolved successfully for live session: {}", liveSessionId);

        return reportMapper.toDto(saved);
    }

    @Override
    public List<LiveSessionReportDto> getMyCreatedReports() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching reports created by user ID: {}", currentUserId);

        return reportRepository.findByReporterId(currentUserId).stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LiveSessionReportDto> getMyReceivedReports() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching reports filed against user ID: {}", currentUserId);

        return reportRepository.findByReportedUserId(currentUserId).stream()
                .map(reportMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Send email notifications to both trainee (reporter) and coach (reported user)
     */
    private void sendReportNotificationEmails(LiveSessionReport report, CoachBooking booking) {
        try {
            // Get trainee and coach emails
            String traineeEmail = booking.getTrainee().getAccount().getEmail();
            String coachEmail = booking.getCoach().getAccount().getEmail();

            String traineeName = booking.getTrainee().getFullName();
            String coachName = booking.getCoach().getFullName();

            // Send email to trainee (reporter)
            emailService.sendReportCreatedNotificationToReporter(
                    traineeEmail,
                    traineeName,
                    report
            );

            // Send email to coach (reported user)
            emailService.sendReportCreatedNotificationToReportedUser(
                    coachEmail,
                    coachName,
                    report
            );

            log.info("Report notification emails sent for report ID: {}", report.getLiveSessionId());
        } catch (Exception e) {
            log.error("Failed to send report notification emails", e);
            // Don't throw - report creation should not fail if email fails
        }
    }
}



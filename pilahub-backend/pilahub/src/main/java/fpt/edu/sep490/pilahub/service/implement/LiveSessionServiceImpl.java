package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.config.properties.AgoraProperties;
import fpt.edu.sep490.pilahub.dto.CoachBookingDto;
import fpt.edu.sep490.pilahub.dto.LiveSessionDto;
import fpt.edu.sep490.pilahub.dto.LiveSessionTokenDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.enums.BookingStatus;
import fpt.edu.sep490.pilahub.enums.BookingType;
import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LiveSessionMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import fpt.edu.sep490.pilahub.repository.CoachBookingRepository;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.repository.PersonalScheduleRepository;
import fpt.edu.sep490.pilahub.service.AgoraRecordingService;
import fpt.edu.sep490.pilahub.service.LiveSessionService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import io.agora.media.RtcTokenBuilder2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LiveSessionServiceImpl implements LiveSessionService {

    private final LiveSessionRepository liveSessionRepository;
    private final CoachBookingRepository coachBookingRepository;
    private final LiveSessionMapper liveSessionMapper;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final PersonalScheduleMapper personalScheduleMapper;
    private final AgoraProperties agoraProperties;
    private final SecurityUtil securityUtil;
    private final AgoraRecordingService agoraRecordingService;

    private static final int TOKEN_GENERATION_MINUTES_BEFORE = 10; // Generate token 10 minutes before start
    private static final int NO_SHOW_TIMEOUT_MINUTES = 15; // No-show after 15 minutes

    @Override
    public LiveSessionDto createLiveSession(UUID bookingId) {
        log.info("Creating live session for booking ID: {}", bookingId);

        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        // Check if live session already exists
        if (liveSessionRepository.existsByLiveSessionId(bookingId)) {
            throw new InvalidRequestException("Live session already exists for this booking");
        }

        // Verify booking status is SCHEDULED (after payment)
        if (booking.getStatus() != BookingStatus.SCHEDULED) {
            throw new InvalidRequestException("Booking must be in SCHEDULED status to create live session");
        }

        // Generate channel name and UIDs
        String channelName = "session_" + bookingId.toString().substring(0, 8);
        Integer coachUid = generateUid(booking.getCoach().getCoachId());
        Integer traineeUid = generateUid(booking.getTrainee().getTraineeId());

        // Create LiveSession - DO NOT manually set liveSessionId, @MapsId will handle
        // it
        LiveSession liveSession = new LiveSession();
        liveSession.setCoachBooking(booking); // Must set this first for @MapsId
        liveSession.setChannelName(channelName);
        liveSession.setCoachUid(coachUid);
        liveSession.setTraineeUid(traineeUid);
        liveSession.setStatus(LiveSessionStatus.PENDING);
        liveSession.setRecordingEnabled(true);

        LiveSession saved = liveSessionRepository.save(liveSession);
        log.info("Live session created successfully: {}", saved.getLiveSessionId());

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public LiveSessionDto getLiveSessionById(UUID liveSessionId) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        return toDto(liveSession);
    }

    @Override
    @Transactional(readOnly = true)
    public LiveSessionDto getLiveSessionByBookingId(UUID bookingId) {
        return getLiveSessionById(bookingId); // Same thing since IDs match
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDto> getCoachSessions(UUID coachId) {
        return liveSessionRepository.findByCoachId(coachId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDto> getTraineeSessions(UUID traineeId) {
        return liveSessionRepository.findByTraineeId(traineeId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LiveSessionDto> getMySessions() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        if (currentRole == Role.COACH) {
            return getCoachSessions(currentUserId);
        } else if (currentRole == Role.TRAINEE) {
            return getTraineeSessions(currentUserId);
        } else {
            throw new AccessDeniedException("Only coaches and trainees can view sessions");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public LiveSessionTokenDto getMySessionToken(UUID liveSessionId) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        // Verify session is ACTIVE
        if (liveSession.getStatus() != LiveSessionStatus.ACTIVE) {
            throw new InvalidRequestException("Session is not active. Current status: " + liveSession.getStatus());
        }

        // Verify user is part of this session
        UUID coachId = liveSession.getCoachBooking().getCoach().getCoachId();
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();

        boolean isCoach = currentRole == Role.COACH && currentUserId.equals(coachId);
        boolean isTrainee = currentRole == Role.TRAINEE && currentUserId.equals(traineeId);

        if (!isCoach && !isTrainee) {
            throw new AccessDeniedException("You are not authorized to access this session");
        }

        // Check if tokens exist, if not generate them
        if (liveSession.getCoachToken() == null || liveSession.getTraineeToken() == null) {
            generateTokensForSession(liveSession);
            liveSession = liveSessionRepository.save(liveSession);
        }

        // Return appropriate token
        String token = isCoach ? liveSession.getCoachToken() : liveSession.getTraineeToken();
        Integer uid = isCoach ? liveSession.getCoachUid() : liveSession.getTraineeUid();

        return new LiveSessionTokenDto(
                liveSession.getChannelName(),
                uid,
                token,
                agoraProperties.getTokenExpirationSeconds());
    }

    @Override
    public LiveSessionDto markAsJoined(UUID liveSessionId) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        UUID coachId = liveSession.getCoachBooking().getCoach().getCoachId();
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();

        Instant now = Instant.now();

        if (currentRole == Role.COACH && currentUserId.equals(coachId)) {
            if (liveSession.getCoachJoinedAt() == null) {
                liveSession.setCoachJoinedAt(now);
                log.info("Coach joined session: {}", liveSessionId);
            }
        } else if (currentRole == Role.TRAINEE && currentUserId.equals(traineeId)) {
            if (liveSession.getTraineeJoinedAt() == null) {
                liveSession.setTraineeJoinedAt(now);
                log.info("Trainee joined session: {}", liveSessionId);
            }
        } else {
            throw new AccessDeniedException("You are not authorized to join this session");
        }

        // Check if both joined -> update booking to IN_PROGRESS and start recording
        if (liveSession.getCoachJoinedAt() != null && liveSession.getTraineeJoinedAt() != null) {
            CoachBooking booking = liveSession.getCoachBooking();
            if (booking.getStatus() == BookingStatus.READY) {
                booking.setStatus(BookingStatus.IN_PROGRESS);
                coachBookingRepository.save(booking);
                log.info("Booking {} status updated to IN_PROGRESS", booking.getId());

                // Auto start recording when first person joins
                if (liveSession.isRecordingEnabled() && liveSession.getAgoraResourceId() == null) {
                    try {
                        agoraRecordingService.startRecording(liveSessionId);
                        log.info("Cloud recording started for session: {}", liveSessionId);
                    } catch (Exception e) {
                        log.error("Failed to start recording for session {}: {}", liveSessionId, e.getMessage());
                        // Don't fail the join - recording is optional
                        liveSession.setRecordingEnabled(false);
                        liveSession.setErrorMessage("Recording failed: " + e.getMessage());
                    }
                }
            }
        }

        LiveSession saved = liveSessionRepository.save(liveSession);
        return toDto(saved);
    }

    @Override
    public LiveSessionDto markAsLeft(UUID liveSessionId) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        UUID coachId = liveSession.getCoachBooking().getCoach().getCoachId();
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();

        boolean isCoach = currentRole == Role.COACH && currentUserId.equals(coachId);
        boolean isTrainee = currentRole == Role.TRAINEE && currentUserId.equals(traineeId);

        if (!isCoach && !isTrainee) {
            throw new AccessDeniedException("You are not authorized to leave this session");
        }

        Instant now = Instant.now();

        // Mark as left based on role
        if (isCoach && liveSession.getCoachJoinedAt() != null) {
            liveSession.setCoachJoinedAt(null); // Clear joined timestamp
            log.info("Coach left session: {}", liveSessionId);
        } else if (isTrainee && liveSession.getTraineeJoinedAt() != null) {
            liveSession.setTraineeJoinedAt(null); // Clear joined timestamp
            log.info("Trainee left session: {}", liveSessionId);
        }

        // Check if room is empty (both users have left)
        boolean roomIsEmpty = liveSession.getCoachJoinedAt() == null &&
                liveSession.getTraineeJoinedAt() == null;
        CoachBooking booking = liveSession.getCoachBooking();

        // Only auto-complete by "both left" after session actually started.
        if (roomIsEmpty && booking.getStatus() == BookingStatus.IN_PROGRESS) {
            log.info("Room is empty (both users left) - stopping recording and ending session: {}", liveSessionId);

            // Stop recording if it's running
            if (liveSession.getAgoraResourceId() != null && liveSession.getAgoraRecordingSid() != null) {
                try {
                    agoraRecordingService.stopRecording(liveSessionId);
                    log.info("Cloud recording stopped for session: {}", liveSessionId);
                } catch (Exception e) {
                    log.error("Failed to stop recording for session {}: {}", liveSessionId, e.getMessage());
                }
            }

            // End session
            liveSession.setStatus(LiveSessionStatus.COMPLETED);
            liveSession.setSessionEndedAt(now);

            // Update booking status
            booking.setStatus(BookingStatus.COMPLETED);
            coachBookingRepository.save(booking);

            log.info("Session {} auto-ended - both users left", liveSessionId);
        }

        LiveSession saved = liveSessionRepository.save(liveSession);
        return toDto(saved);
    }

    @Override
    public LiveSessionDto endSession(UUID liveSessionId) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        UUID coachId = liveSession.getCoachBooking().getCoach().getCoachId();
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();

        // Only coach or trainee can end their own session
        boolean isAuthorized = (currentRole == Role.COACH && currentUserId.equals(coachId)) ||
                (currentRole == Role.TRAINEE && currentUserId.equals(traineeId));

        if (!isAuthorized) {
            throw new AccessDeniedException("You are not authorized to end this session");
        }

        if (liveSession.getStatus() != LiveSessionStatus.ACTIVE) {
            throw new InvalidRequestException("Can only end active sessions");
        }

        liveSession.setStatus(LiveSessionStatus.COMPLETED);
        liveSession.setSessionEndedAt(Instant.now());

        // Stop recording if it's running
        if (liveSession.getAgoraResourceId() != null && liveSession.getAgoraRecordingSid() != null) {
            try {
                agoraRecordingService.stopRecording(liveSessionId);
                log.info("Cloud recording stopped for session: {}", liveSessionId);
            } catch (Exception e) {
                log.error("Failed to stop recording for session {}: {}", liveSessionId, e.getMessage());
                // Don't fail the end session - just log the error
            }
        }

        // Update booking status
        CoachBooking booking = liveSession.getCoachBooking();
        booking.setStatus(BookingStatus.COMPLETED);
        coachBookingRepository.save(booking);

        LiveSession saved = liveSessionRepository.save(liveSession);
        log.info("Session {} ended manually by user {}", liveSessionId, currentUserId);

        return toDto(saved);
    }

    @Override
    public void generateTokensForUpcomingSessions() {
        Instant now = Instant.now();
        Instant generationWindow = now.plus(TOKEN_GENERATION_MINUTES_BEFORE, ChronoUnit.MINUTES);

        // Find PENDING sessions that are about to start
        List<LiveSession> sessions = liveSessionRepository.findByStatusAndStartTimeBefore(
                LiveSessionStatus.PENDING, generationWindow);

        for (LiveSession session : sessions) {
            try {
                // Only generate if within 10 minutes of start time
                Instant startTime = session.getCoachBooking().getStartTime();
                long minutesUntilStart = ChronoUnit.MINUTES.between(now, startTime);

                if (minutesUntilStart <= TOKEN_GENERATION_MINUTES_BEFORE && minutesUntilStart >= 0) {
                    generateTokensForSession(session);
                    liveSessionRepository.save(session);
                    log.info("Tokens generated for session: {}", session.getLiveSessionId());
                }
            } catch (Exception e) {
                log.error("Failed to generate tokens for session {}: {}", session.getLiveSessionId(), e.getMessage());
                session.setStatus(LiveSessionStatus.FAILED);
                session.setErrorMessage("Token generation failed: " + e.getMessage());
                liveSessionRepository.save(session);
            }
        }
    }

    @Override
    public void updateSessionStatusFromBooking() {
        Instant now = Instant.now();

        // Find PENDING sessions where booking has become READY
        List<CoachBooking> readyBookings = coachBookingRepository.findScheduledBookingsReadyToStart(now);

        for (CoachBooking booking : readyBookings) {
            try {
                // Update booking to READY
                if (booking.getStatus() == BookingStatus.SCHEDULED) {
                    booking.setStatus(BookingStatus.READY);
                    coachBookingRepository.save(booking);
                    activateSessionForReadyBooking(booking.getId());
                }
            } catch (Exception e) {
                log.error("Failed to update session status for booking {}: {}", booking.getId(), e.getMessage());
            }
        }
    }

    @Override
    public void activateSessionForReadyBooking(UUID bookingId) {
        CoachBooking booking = coachBookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("CoachBooking", "id", bookingId));

        if (booking.getStatus() != BookingStatus.READY) {
            log.warn("Skip live session activation because booking {} is {} instead of READY", bookingId, booking.getStatus());
            return;
        }

        liveSessionRepository.findById(bookingId).ifPresentOrElse(session -> {
            if (session.getStatus() == LiveSessionStatus.PENDING) {
                session.setStatus(LiveSessionStatus.ACTIVE);
            }

            if (session.getCoachToken() == null || session.getTraineeToken() == null) {
                generateTokensForSession(session);
            }

            liveSessionRepository.save(session);
            log.info("Session {} synced for READY booking {} (session status: {})",
                    session.getLiveSessionId(), bookingId, session.getStatus());
        }, () -> log.warn("Live session not found for READY booking {}", bookingId));
    }

    @Override
    public void checkAndHandleNoShow() {
        Instant now = Instant.now();
        Instant noShowThreshold = now.minus(NO_SHOW_TIMEOUT_MINUTES, ChronoUnit.MINUTES);

        List<LiveSession> sessions = liveSessionRepository.findActiveSessionsWithMissingParticipants(noShowThreshold);

        for (LiveSession session : sessions) {
            try {
                boolean coachJoined = session.getCoachJoinedAt() != null;
                boolean traineeJoined = session.getTraineeJoinedAt() != null;

                CoachBooking booking = session.getCoachBooking();

                if (!coachJoined && !traineeJoined) {
                    // Neither joined - mark as NO_SHOW (both)
                    session.setStatus(LiveSessionStatus.NO_SHOW);
                    booking.setStatus(BookingStatus.NO_SHOW_BY_COACH); // Default to coach no-show for refund
                    log.info("Session {} marked as NO_SHOW - neither party joined", session.getLiveSessionId());
                } else if (!coachJoined) {
                    // Coach didn't join
                    session.setStatus(LiveSessionStatus.NO_SHOW);
                    booking.setStatus(BookingStatus.NO_SHOW_BY_COACH);
                    log.info("Session {} marked as NO_SHOW - coach didn't join", session.getLiveSessionId());
                } else if (!traineeJoined) {
                    // Trainee didn't join
                    session.setStatus(LiveSessionStatus.NO_SHOW);
                    booking.setStatus(BookingStatus.NO_SHOW_BY_TRAINEE);
                    log.info("Session {} marked as NO_SHOW - trainee didn't join", session.getLiveSessionId());
                }

                session.setSessionEndedAt(now);
                liveSessionRepository.save(session);
                coachBookingRepository.save(booking);

            } catch (Exception e) {
                log.error("Failed to handle no-show for session {}: {}", session.getLiveSessionId(), e.getMessage());
            }
        }
    }

    @Override
    public void completeEndedSessions() {
        Instant now = Instant.now();

        List<LiveSession> sessions = liveSessionRepository.findActiveSessionsEndingBefore(now);

        for (LiveSession session : sessions) {
            try {
                session.setStatus(LiveSessionStatus.COMPLETED);
                session.setSessionEndedAt(now);

                // Stop recording if it's running
                if (session.getAgoraResourceId() != null && session.getAgoraRecordingSid() != null) {
                    try {
                        agoraRecordingService.stopRecording(session.getLiveSessionId());
                        log.info("Cloud recording stopped for session: {}", session.getLiveSessionId());
                    } catch (Exception e) {
                        log.error("Failed to stop recording: {}", e.getMessage());
                    }
                }

                CoachBooking booking = session.getCoachBooking();
                if (booking.getStatus() == BookingStatus.IN_PROGRESS || booking.getStatus() == BookingStatus.READY) {
                    booking.setStatus(BookingStatus.COMPLETED);
                    coachBookingRepository.save(booking);
                }

                liveSessionRepository.save(session);
                log.info("Session {} completed automatically", session.getLiveSessionId());

            } catch (Exception e) {
                log.error("Failed to complete session {}: {}", session.getLiveSessionId(), e.getMessage());
            }
        }
    }

    // ============= PRIVATE HELPER METHODS =============

    private void generateTokensForSession(LiveSession session) {
        log.info("Generating Agora tokens for session {}", session.getLiveSessionId());

        // Validate App Certificate
        String appCert = agoraProperties.getAppCertificate();
        if (appCert == null || appCert.isEmpty()) {
            log.error("Agora App Certificate is not configured!");
            throw new InvalidRequestException(
                    "Agora App Certificate is not configured! Check application.yml or environment variables.");
        }
        if (appCert.equals(agoraProperties.getAppId())) {
            log.error("App Certificate equals App ID - This is incorrect!");
            throw new InvalidRequestException("App Certificate must be DIFFERENT from App ID!");
        }

        int expirationTs = (int) (Instant.now().getEpochSecond() + agoraProperties.getTokenExpirationSeconds());

        // Generate tokens using Agora Official SDK (Version 007)
        RtcTokenBuilder2 tokenBuilder = new RtcTokenBuilder2();

        try {
            String coachToken = tokenBuilder.buildTokenWithUid(
                    agoraProperties.getAppId(),
                    agoraProperties.getAppCertificate(),
                    session.getChannelName(),
                    session.getCoachUid(),
                    RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    expirationTs,
                    expirationTs);

            String traineeToken = tokenBuilder.buildTokenWithUid(
                    agoraProperties.getAppId(),
                    agoraProperties.getAppCertificate(),
                    session.getChannelName(),
                    session.getTraineeUid(),
                    RtcTokenBuilder2.Role.ROLE_PUBLISHER,
                    expirationTs,
                    expirationTs);

            session.setCoachToken(coachToken);
            session.setTraineeToken(traineeToken);
            session.setTokenGeneratedAt(Instant.now());
            session.setTokenExpiresAt(Instant.ofEpochSecond(expirationTs));

            log.info("Tokens generated successfully for session {}", session.getLiveSessionId());

        } catch (Exception e) {
            log.error("Failed to generate Agora tokens: {}", e.getMessage(), e);
            throw new InvalidRequestException("Failed to generate Agora tokens: " + e.getMessage());
        }
    }

    @Override
    public LiveSessionDto toggleRecording(UUID liveSessionId, boolean enabled) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);

        liveSession.setRecordingEnabled(enabled);
        LiveSession saved = liveSessionRepository.save(liveSession);

        log.info("Recording {} for session {}", enabled ? "enabled" : "disabled", liveSessionId);

        return toDto(saved);
    }

    @Override
    public LiveSessionDto submitTraineeRating(UUID liveSessionId, java.math.BigDecimal rating) {
        LiveSession liveSession = findLiveSessionOrThrow(liveSessionId);
        UUID currentUserId = securityUtil.getCurrentUserId();
        Role currentRole = securityUtil.getCurrentUserRole();

        // Verify user is the trainee
        UUID traineeId = liveSession.getCoachBooking().getTrainee().getTraineeId();
        if (currentRole != Role.TRAINEE || !currentUserId.equals(traineeId)) {
            throw new AccessDeniedException("Only the trainee of this session can submit a rating");
        }

        // Verify session is completed
        if (liveSession.getStatus() != LiveSessionStatus.COMPLETED) {
            throw new InvalidRequestException(
                    "Can only rate completed sessions. Current status: " + liveSession.getStatus());
        }

        // Verify rating hasn't been submitted yet
        if (liveSession.getRatingByTrainee() != null) {
            throw new InvalidRequestException("Rating has already been submitted for this session");
        }

        // Validate rating is in 0.5 increments
        double ratingValue = rating.doubleValue();
        if ((ratingValue * 10) % 5 != 0) {
            throw new InvalidRequestException("Rating must be in 0.5 increments (e.g., 0.5, 1.0, 1.5, ... 5.0)");
        }

        liveSession.setRatingByTrainee(rating);
        LiveSession saved = liveSessionRepository.save(liveSession);

        log.info("Trainee {} submitted rating {} for session {}", currentUserId, rating, liveSessionId);

        return toDto(saved);
    }

    private LiveSession findLiveSessionOrThrow(UUID liveSessionId) {
        return liveSessionRepository.findById(liveSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("LiveSession", "id", liveSessionId));
    }

    private Integer generateUid(UUID accountId) {
        // Generate a positive integer UID from UUID
        // Use hashCode and ensure it's positive
        int hash = accountId.hashCode();
        return Math.abs(hash) % 1000000000; // Ensure it's within Agora's UID range
    }

    private LiveSessionDto toDto(LiveSession session) {
        LiveSessionDto dto = liveSessionMapper.toDto(session);
        CoachBooking booking = session.getCoachBooking();
        if (booking.getBookingType() == BookingType.PERSONAL_TRAINING_PACKAGE
                && booking.getRecurringGroupId() != null) {
            Instant startOfDay = booking.getStartTime().truncatedTo(ChronoUnit.DAYS);
            Instant endOfDay = startOfDay.plus(1, ChronoUnit.DAYS);
            PersonalScheduleDto scheduleDto = personalScheduleRepository
                    .findByRoadmapIdAndDate(booking.getRecurringGroupId(), startOfDay, endOfDay)
                    .map(personalScheduleMapper::toDto)
                    .orElse(null);
            CoachBookingDto enrichedBooking = new CoachBookingDto(
                    dto.coachBooking().id(), dto.coachBooking().coach(), dto.coachBooking().trainee(),
                    dto.coachBooking().startTime(), dto.coachBooking().endTime(),
                    dto.coachBooking().pricePerHour(), dto.coachBooking().totalAmount(),
                    dto.coachBooking().status(), dto.coachBooking().bookingType(),
                    dto.coachBooking().recurringGroupId(), dto.coachBooking().createdAt(), scheduleDto);
            return new LiveSessionDto(
                    dto.liveSessionId(), enrichedBooking, dto.channelName(), dto.coachUid(), dto.traineeUid(),
                    dto.coachToken(), dto.traineeToken(), dto.tokenGeneratedAt(), dto.tokenExpiresAt(),
                    dto.status(), dto.coachJoinedAt(), dto.traineeJoinedAt(), dto.sessionEndedAt(),
                    dto.recordingEnabled(), dto.agoraResourceId(), dto.agoraRecordingSid(), dto.recordingUrl(),
                    dto.recordingExpiresAt(), dto.ratingByTrainee(),
                    dto.errorMessage(), dto.createdAt());
        }
        return dto;
    }
}

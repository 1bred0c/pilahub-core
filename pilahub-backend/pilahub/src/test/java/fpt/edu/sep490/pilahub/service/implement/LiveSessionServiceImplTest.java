package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.config.properties.AgoraProperties;
import fpt.edu.sep490.pilahub.dto.LiveSessionTokenDto;
import fpt.edu.sep490.pilahub.enums.LiveSessionStatus;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.LiveSessionMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.pojo.CoachBooking;
import fpt.edu.sep490.pilahub.pojo.LiveSession;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.CoachBookingRepository;
import fpt.edu.sep490.pilahub.repository.LiveSessionRepository;
import fpt.edu.sep490.pilahub.repository.PersonalScheduleRepository;
import fpt.edu.sep490.pilahub.service.AgoraRecordingService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LiveSessionServiceImplTest {

    @Mock
    private LiveSessionRepository liveSessionRepository;

    @Mock
    private CoachBookingRepository coachBookingRepository;

    @Mock
    private LiveSessionMapper liveSessionMapper;

    @Mock
    private PersonalScheduleRepository personalScheduleRepository;

    @Mock
    private PersonalScheduleMapper personalScheduleMapper;

    @Mock
    private AgoraProperties agoraProperties;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private AgoraRecordingService agoraRecordingService;

    @InjectMocks
    private LiveSessionServiceImpl liveSessionService;

    private UUID liveSessionId;
    private UUID coachId;
    private UUID traineeId;

    @BeforeEach
    void setUp() {
        liveSessionId = UUID.randomUUID();
        coachId = UUID.randomUUID();
        traineeId = UUID.randomUUID();
    }

    @Test
    void testUTCID01_GetMySessionToken_AsCoach_WithExistingTokens_ReturnsCoachToken() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.ACTIVE, "coach-token", "trainee-token");
        when(agoraProperties.getTokenExpirationSeconds()).thenReturn(3600);
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(securityUtil.getCurrentUserId()).thenReturn(coachId);
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.COACH);

        // Act
        LiveSessionTokenDto result = liveSessionService.getMySessionToken(liveSessionId);

        // Assert
        assertEquals(liveSession.getChannelName(), result.channelName());
        assertEquals(liveSession.getCoachUid(), result.uid());
        assertEquals("coach-token", result.token());
        assertEquals(3600, result.expirationSeconds());
        verify(liveSessionRepository, never()).save(liveSession);
    }

    @Test
    void testUTCID02_GetMySessionToken_AsTrainee_WithExistingTokens_ReturnsTraineeToken() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.ACTIVE, "coach-token", "trainee-token");
        when(agoraProperties.getTokenExpirationSeconds()).thenReturn(3600);
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(securityUtil.getCurrentUserId()).thenReturn(traineeId);
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.TRAINEE);

        // Act
        LiveSessionTokenDto result = liveSessionService.getMySessionToken(liveSessionId);

        // Assert
        assertEquals(liveSession.getChannelName(), result.channelName());
        assertEquals(liveSession.getTraineeUid(), result.uid());
        assertEquals("trainee-token", result.token());
        assertEquals(3600, result.expirationSeconds());
        verify(liveSessionRepository, never()).save(liveSession);
    }

    @Test
    void testUTCID03_GetMySessionToken_WhenSessionNotFound_ThrowsResourceNotFoundException() {
        // Arrange
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.empty());

        // Act
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> liveSessionService.getMySessionToken(liveSessionId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("LiveSession"));
    }

    @Test
    void testUTCID04_GetMySessionToken_WhenSessionNotActive_ThrowsInvalidRequestException() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.PENDING, "coach-token", "trainee-token");
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(securityUtil.getCurrentUserId()).thenReturn(coachId);
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.COACH);

        // Act
        InvalidRequestException exception = assertThrows(
                InvalidRequestException.class,
                () -> liveSessionService.getMySessionToken(liveSessionId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Session is not active"));
        verify(liveSessionRepository, never()).save(liveSession);
    }

    @Test
    void testUTCID05_GetMySessionToken_WhenUserNotInSession_ThrowsAccessDeniedException() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.ACTIVE, "coach-token", "trainee-token");
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(securityUtil.getCurrentUserId()).thenReturn(UUID.randomUUID());
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.TRAINEE);

        // Act
        AccessDeniedException exception = assertThrows(
                AccessDeniedException.class,
                () -> liveSessionService.getMySessionToken(liveSessionId)
        );

        // Assert
        assertTrue(exception.getMessage().contains("not authorized"));
        verify(liveSessionRepository, never()).save(liveSession);
    }

    @Test
    void testUTCID06_GetMySessionToken_WhenTokenMissing_GeneratesAndSavesThenReturnsCoachToken() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.ACTIVE, null, "existing-trainee-token");
        when(agoraProperties.getTokenExpirationSeconds()).thenReturn(3600);
        when(agoraProperties.getAppId()).thenReturn("0123456789abcdef0123456789abcdef");
        when(agoraProperties.getAppCertificate()).thenReturn("abcdef0123456789abcdef0123456789");
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(liveSessionRepository.save(liveSession)).thenReturn(liveSession);
        when(securityUtil.getCurrentUserId()).thenReturn(coachId);
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.COACH);

        // Act
        LiveSessionTokenDto result = liveSessionService.getMySessionToken(liveSessionId);

        // Assert
        assertEquals(liveSession.getChannelName(), result.channelName());
        assertEquals(liveSession.getCoachUid(), result.uid());
        assertNotNull(result.token());
        assertEquals(3600, result.expirationSeconds());
        verify(liveSessionRepository).save(liveSession);
        assertNotNull(liveSession.getCoachToken());
        assertNotNull(liveSession.getTraineeToken());
        assertNotNull(liveSession.getTokenGeneratedAt());
        assertNotNull(liveSession.getTokenExpiresAt());
    }

    @Test
    void testUTCID07_GetMySessionToken_WhenTraineeTokenMissing_GeneratesAndReturnsTraineeToken() {
        // Arrange
        LiveSession liveSession = buildLiveSession(LiveSessionStatus.ACTIVE, "existing-coach-token", null);
        when(agoraProperties.getTokenExpirationSeconds()).thenReturn(3600);
        when(agoraProperties.getAppId()).thenReturn("0123456789abcdef0123456789abcdef");
        when(agoraProperties.getAppCertificate()).thenReturn("abcdef0123456789abcdef0123456789");
        when(liveSessionRepository.findById(liveSessionId)).thenReturn(Optional.of(liveSession));
        when(liveSessionRepository.save(liveSession)).thenReturn(liveSession);
        when(securityUtil.getCurrentUserId()).thenReturn(traineeId);
        when(securityUtil.getCurrentUserRole()).thenReturn(Role.TRAINEE);

        // Act
        LiveSessionTokenDto result = liveSessionService.getMySessionToken(liveSessionId);

        // Assert
        assertEquals(liveSession.getChannelName(), result.channelName());
        assertEquals(liveSession.getTraineeUid(), result.uid());
        assertNotNull(result.token());
        assertEquals(3600, result.expirationSeconds());
        verify(liveSessionRepository).save(liveSession);
        assertNotNull(liveSession.getCoachToken());
        assertNotNull(liveSession.getTraineeToken());
    }

    private LiveSession buildLiveSession(LiveSessionStatus status, String coachToken, String traineeToken) {
        Coach coach = new Coach();
        coach.setCoachId(coachId);

        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);

        CoachBooking coachBooking = new CoachBooking();
        coachBooking.setCoach(coach);
        coachBooking.setTrainee(trainee);

        LiveSession liveSession = new LiveSession();
        liveSession.setLiveSessionId(liveSessionId);
        liveSession.setCoachBooking(coachBooking);
        liveSession.setStatus(status);
        liveSession.setChannelName("session_test");
        liveSession.setCoachUid(11111);
        liveSession.setTraineeUid(22222);
        liveSession.setCoachToken(coachToken);
        liveSession.setTraineeToken(traineeToken);

        return liveSession;
    }
}



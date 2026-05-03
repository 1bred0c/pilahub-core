package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import fpt.edu.sep490.pilahub.dto.request.CreateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.response.InBodyExtractApiResponse;
import fpt.edu.sep490.pilahub.dto.response.InBodyExtractData;
import fpt.edu.sep490.pilahub.enums.ProfileSource;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.HealthProfileMapper;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.HealthProfileRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.HealthProfileAssessmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthProfileServiceImplTest {

    @Mock
    private HealthProfileRepository healthProfileRepository;

    @Mock
    private TraineeRepository traineeRepository;

    @Mock
    private HealthProfileMapper healthProfileMapper;

    @Mock
    private HealthProfileAssessmentService assessmentService;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private HealthProfileServiceImpl healthProfileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(healthProfileService, "aiServerUrl", "http://ai.test");
        ReflectionTestUtils.setField(healthProfileService, "aiInBodyExtractEndpoint", "/inbody");
    }

    @Test
    void testUTCID01_CreateSuccessfully_NoPreviousLatest_AssessmentSuccess() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();
        CreateHealthProfileRequest request = buildRequest();

        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);

        HealthProfile mappedProfile = new HealthProfile();
        mappedProfile.setSource(ProfileSource.Manual);

        HealthProfile savedProfile = new HealthProfile();
        savedProfile.setHealthProfileId(profileId);
        savedProfile.setTrainee(trainee);
        savedProfile.setLatest(true);

        HealthProfileDto expectedDto = new HealthProfileDto(
                profileId,
                traineeId,
                request.heightCm(),
                request.weightKg(),
                request.bmi(),
                request.bodyFatPercentage(),
                request.muscleMassKg(),
                request.waistCm(),
                request.hipCm(),
                request.source(),
                request.metadata(),
                true,
                Instant.now()
        );

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(healthProfileRepository.findLatestByTraineeId(traineeId)).thenReturn(Optional.empty());
        when(healthProfileMapper.toEntity(request)).thenReturn(mappedProfile);
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(invocation -> {
            HealthProfile profile = invocation.getArgument(0);
            return profile == mappedProfile ? savedProfile : profile;
        });
        when(healthProfileMapper.toDto(savedProfile)).thenReturn(expectedDto);

        // Act
        HealthProfileDto actual = healthProfileService.createHealthProfile(traineeId, request);

        // Assert
        assertSame(expectedDto, actual);
        assertSame(trainee, mappedProfile.getTrainee());
        assertTrue(mappedProfile.isLatest());

        verify(traineeRepository).findById(traineeId);
        verify(healthProfileRepository).findLatestByTraineeId(traineeId);
        verify(healthProfileRepository).save(mappedProfile);
        verify(healthProfileRepository).flush();
        verify(assessmentService).createAssessment(savedProfile);
        verify(healthProfileMapper).toDto(savedProfile);
    }

    @Test
    void testUTCID02_CreateSuccessfully_WithPreviousLatest_ProfileIsUpdatedToNotLatest() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        CreateHealthProfileRequest request = buildRequest();

        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);

        HealthProfile previousLatest = new HealthProfile();
        previousLatest.setLatest(true);

        HealthProfile mappedProfile = new HealthProfile();
        mappedProfile.setSource(ProfileSource.Manual);

        HealthProfile savedProfile = new HealthProfile();
        savedProfile.setHealthProfileId(UUID.randomUUID());
        savedProfile.setTrainee(trainee);
        savedProfile.setLatest(true);

        HealthProfileDto expectedDto = new HealthProfileDto(
                savedProfile.getHealthProfileId(),
                traineeId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProfileSource.Manual,
                null,
                true,
                Instant.now()
        );

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(healthProfileRepository.findLatestByTraineeId(traineeId)).thenReturn(Optional.of(previousLatest));
        when(healthProfileMapper.toEntity(request)).thenReturn(mappedProfile);
        when(healthProfileRepository.save(any(HealthProfile.class))).thenAnswer(invocation -> {
            HealthProfile profile = invocation.getArgument(0);
            return profile == mappedProfile ? savedProfile : profile;
        });
        when(healthProfileMapper.toDto(savedProfile)).thenReturn(expectedDto);

        // Act
        HealthProfileDto actual = healthProfileService.createHealthProfile(traineeId, request);

        // Assert
        assertSame(expectedDto, actual);
        assertFalse(previousLatest.isLatest());
        assertTrue(mappedProfile.isLatest());
        assertSame(trainee, mappedProfile.getTrainee());

        verify(healthProfileRepository, times(2)).save(any(HealthProfile.class));
        verify(healthProfileRepository).save(previousLatest);
        verify(healthProfileRepository).save(mappedProfile);
        verify(healthProfileRepository).flush();
        verify(assessmentService).createAssessment(savedProfile);

        ArgumentCaptor<HealthProfile> profileCaptor = ArgumentCaptor.forClass(HealthProfile.class);
        verify(healthProfileRepository, times(2)).save(profileCaptor.capture());
        assertEquals(previousLatest, profileCaptor.getAllValues().get(0));
        assertEquals(mappedProfile, profileCaptor.getAllValues().get(1));
    }

    @Test
    void testUTCID03_TraineeNotFound_ThrowsTraineeNotFoundException() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        CreateHealthProfileRequest request = buildRequest();
        when(traineeRepository.findById(traineeId)).thenReturn(Optional.empty());

        // Act
        TraineeNotFoundException exception = assertThrows(
                TraineeNotFoundException.class,
                () -> healthProfileService.createHealthProfile(traineeId, request)
        );

        // Assert
        assertTrue(exception.getMessage().contains(traineeId.toString()));
        verify(traineeRepository).findById(traineeId);
        verifyNoInteractions(healthProfileRepository, healthProfileMapper, assessmentService);
    }

    @Test
    void testUTCID04_AssessmentServiceThrows_ExceptionIsSwallowed_AndProfileStillReturned() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        CreateHealthProfileRequest request = buildRequest();

        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);

        HealthProfile mappedProfile = new HealthProfile();
        HealthProfile savedProfile = new HealthProfile();
        savedProfile.setHealthProfileId(UUID.randomUUID());

        HealthProfileDto expectedDto = new HealthProfileDto(
                savedProfile.getHealthProfileId(),
                traineeId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                ProfileSource.Manual,
                null,
                true,
                Instant.now()
        );

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(healthProfileRepository.findLatestByTraineeId(traineeId)).thenReturn(Optional.empty());
        when(healthProfileMapper.toEntity(request)).thenReturn(mappedProfile);
        when(healthProfileRepository.save(mappedProfile)).thenReturn(savedProfile);
        doThrow(new RuntimeException("AI service unavailable"))
                .when(assessmentService).createAssessment(savedProfile);
        when(healthProfileMapper.toDto(savedProfile)).thenReturn(expectedDto);

        // Act
        HealthProfileDto actual = healthProfileService.createHealthProfile(traineeId, request);

        // Assert
        assertNotNull(actual);
        assertSame(expectedDto, actual);
        verify(healthProfileRepository).flush();
        verify(assessmentService).createAssessment(savedProfile);
        verify(healthProfileMapper).toDto(savedProfile);
    }

    @Test
    void testUTCID05_NullRequest_MapperReturnsNullEntity_ThrowsNullPointerException() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);

        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(healthProfileRepository.findLatestByTraineeId(traineeId)).thenReturn(Optional.empty());
        when(healthProfileMapper.toEntity(null)).thenReturn(null);

        // Act
        assertThrows(NullPointerException.class,
                () -> healthProfileService.createHealthProfile(traineeId, null));

        // Assert
        verify(traineeRepository).findById(traineeId);
        verify(healthProfileRepository).findLatestByTraineeId(traineeId);
        verify(healthProfileMapper).toEntity(null);
        verify(healthProfileRepository, never()).save(any(HealthProfile.class));
        verify(assessmentService, never()).createAssessment(any(HealthProfile.class));
        verify(healthProfileMapper, never()).toDto(any(HealthProfile.class));
    }

    @Test
    void testUTCID06_ExtractInBodySuccessfully_CreatesHealthProfileAndReturnsDto() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        MockMultipartFile image = new MockMultipartFile("image", "inbody.jpg", "image/jpeg", "binary".getBytes());
        UUID profileId = UUID.randomUUID();
        Trainee trainee = new Trainee();
        trainee.setTraineeId(traineeId);
        HealthProfile mappedProfile = new HealthProfile();
        HealthProfile savedProfile = new HealthProfile();
        savedProfile.setHealthProfileId(profileId);
        savedProfile.setTrainee(trainee);
        HealthProfileDto expectedDto = new HealthProfileDto(
                profileId,
                traineeId,
                BigDecimal.valueOf(173.0),
                BigDecimal.valueOf(98.9),
                BigDecimal.valueOf(33.0),
                BigDecimal.valueOf(19.7),
                BigDecimal.valueOf(46.3),
                null,
                null,
                ProfileSource.InBody,
                "{\"device\":\"InBody 270\"}",
                true,
                Instant.now()
        );

        InBodyExtractData aiData = new InBodyExtractData(
                BigDecimal.valueOf(173.0),
                BigDecimal.valueOf(98.9),
                BigDecimal.valueOf(33.0),
                BigDecimal.valueOf(19.7),
                BigDecimal.valueOf(46.3),
                null,
                null,
                "INBODY_SCAN",
                "{\"device\":\"InBody 270\"}"
        );
        InBodyExtractApiResponse aiResponse = new InBodyExtractApiResponse("success", aiData, "Extracted successfully from InBody 270");

        when(traineeRepository.existsById(traineeId)).thenReturn(true);
        when(traineeRepository.findById(traineeId)).thenReturn(Optional.of(trainee));
        when(healthProfileRepository.findLatestByTraineeId(traineeId)).thenReturn(Optional.empty());
        when(restTemplate.postForEntity(
                org.mockito.ArgumentMatchers.eq("http://ai.test/inbody"),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers.eq(InBodyExtractApiResponse.class)
        )).thenReturn(ResponseEntity.ok(aiResponse));
        when(healthProfileMapper.toEntity(any(CreateHealthProfileRequest.class))).thenReturn(mappedProfile);
        when(healthProfileRepository.save(mappedProfile)).thenReturn(savedProfile);
        when(healthProfileMapper.toDto(savedProfile)).thenReturn(expectedDto);

        // Act
        HealthProfileDto actual = healthProfileService.extractInBodyScan(traineeId, image, "raw-001");

        // Assert
        assertEquals(ProfileSource.InBody, actual.source());
        assertEquals(BigDecimal.valueOf(173.0), actual.heightCm());
        assertEquals(BigDecimal.valueOf(98.9), actual.weightKg());
        verify(assessmentService).createAssessment(savedProfile);
        verify(healthProfileRepository).flush();

        ArgumentCaptor<HttpEntity> requestCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(
                org.mockito.ArgumentMatchers.eq("http://ai.test/inbody"),
                requestCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(InBodyExtractApiResponse.class)
        );

        @SuppressWarnings("unchecked")
        MultiValueMap<String, Object> requestBody = (MultiValueMap<String, Object>) requestCaptor.getValue().getBody();
        assertNotNull(requestBody);
        assertTrue(requestBody.containsKey("image"));
        assertEquals("raw-001", requestBody.getFirst("rawScanId"));

        ArgumentCaptor<CreateHealthProfileRequest> createRequestCaptor = ArgumentCaptor.forClass(CreateHealthProfileRequest.class);
        verify(healthProfileMapper).toEntity(createRequestCaptor.capture());
        CreateHealthProfileRequest capturedCreateRequest = createRequestCaptor.getValue();
        assertEquals(ProfileSource.InBody, capturedCreateRequest.source());
        assertEquals(aiData.metadata(), capturedCreateRequest.metadata());
    }

    @Test
    void testUTCID07_ExtractInBody_WithUnsupportedContentType_ThrowsIllegalArgumentException() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        MockMultipartFile image = new MockMultipartFile("image", "inbody.pdf", "application/pdf", "binary".getBytes());
        when(traineeRepository.existsById(traineeId)).thenReturn(true);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> healthProfileService.extractInBodyScan(traineeId, image, null)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Unsupported image format"));
        verify(restTemplate, never()).postForEntity(any(String.class), any(HttpEntity.class), org.mockito.ArgumentMatchers.eq(InBodyExtractApiResponse.class));
    }

    @Test
    void testUTCID08_ExtractInBody_WhenAiReturns400_ThrowsIllegalArgumentException() {
        // Arrange
        UUID traineeId = UUID.randomUUID();
        MockMultipartFile image = new MockMultipartFile("image", "inbody.jpg", "image/jpeg", "binary".getBytes());
        when(traineeRepository.existsById(traineeId)).thenReturn(true);

        HttpClientErrorException badRequest = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                "{\"detail\":\"missing image\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(restTemplate.postForEntity(
                org.mockito.ArgumentMatchers.eq("http://ai.test/inbody"),
                any(HttpEntity.class),
                org.mockito.ArgumentMatchers.eq(InBodyExtractApiResponse.class)
        )).thenThrow(badRequest);

        // Act
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> healthProfileService.extractInBodyScan(traineeId, image, null)
        );

        // Assert
        assertTrue(exception.getMessage().contains("Invalid InBody scan payload"));
    }

    private CreateHealthProfileRequest buildRequest() {
        return new CreateHealthProfileRequest(
                BigDecimal.valueOf(175),
                BigDecimal.valueOf(70),
                BigDecimal.valueOf(22.86),
                BigDecimal.valueOf(15.5),
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(80),
                BigDecimal.valueOf(95),
                ProfileSource.Manual,
                "{\"device\":\"InBody\"}"
        );
    }
}





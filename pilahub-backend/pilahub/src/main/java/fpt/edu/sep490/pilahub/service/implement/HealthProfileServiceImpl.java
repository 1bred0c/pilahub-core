package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.HealthProfileAssessmentDto;
import fpt.edu.sep490.pilahub.dto.HealthProfileDto;
import fpt.edu.sep490.pilahub.dto.request.CreateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.request.UpdateHealthProfileRequest;
import fpt.edu.sep490.pilahub.dto.response.InBodyExtractApiResponse;
import fpt.edu.sep490.pilahub.dto.response.InBodyExtractData;
import fpt.edu.sep490.pilahub.enums.ProfileSource;
import fpt.edu.sep490.pilahub.dto.response.HealthProfileWithAssessmentResponse;
import fpt.edu.sep490.pilahub.exception.HealthProfileNotFoundException;
import fpt.edu.sep490.pilahub.exception.TraineeNotFoundException;
import fpt.edu.sep490.pilahub.mapper.HealthProfileMapper;
import fpt.edu.sep490.pilahub.pojo.HealthProfile;
import fpt.edu.sep490.pilahub.pojo.Trainee;
import fpt.edu.sep490.pilahub.repository.HealthProfileRepository;
import fpt.edu.sep490.pilahub.repository.TraineeRepository;
import fpt.edu.sep490.pilahub.service.HealthProfileAssessmentService;
import fpt.edu.sep490.pilahub.service.HealthProfileService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HealthProfileServiceImpl implements HealthProfileService {

    private final HealthProfileRepository healthProfileRepository;
    private final TraineeRepository traineeRepository;
    private final HealthProfileMapper healthProfileMapper;
    private final HealthProfileAssessmentService assessmentService;
    private final RestTemplate restTemplate;

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Value("${ai.server.inbody-extract-endpoint:/api/v1/inbody/extract}")
    private String aiInBodyExtractEndpoint;

    private static final long MAX_INBODY_IMAGE_SIZE_BYTES = 10 * 1024 * 1024L;
    private static final Set<String> ALLOWED_INBODY_IMAGE_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            "image/jpg",
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );

    @Override
    public HealthProfileDto createHealthProfile(UUID traineeId, CreateHealthProfileRequest request) {
        log.info("Creating health profile for trainee ID: {}", traineeId);

        Trainee trainee = traineeRepository.findById(traineeId)
                .orElseThrow(() -> {
                    log.error("Trainee not found with ID: {}", traineeId);
                    return new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
                });

        // Set previous profiles to not latest
        healthProfileRepository.findLatestByTraineeId(traineeId).ifPresent(latestProfile -> {
            log.info("Updating previous latest profile to not latest");
            latestProfile.setLatest(false);
            healthProfileRepository.save(latestProfile);
        });

        HealthProfile healthProfile = healthProfileMapper.toEntity(request);
        healthProfile.setTrainee(trainee);
        healthProfile.setLatest(true);

        HealthProfile savedProfile = healthProfileRepository.save(healthProfile);
        // Flush to ensure the health profile is committed to database before creating assessment
        healthProfileRepository.flush();
        log.info("Health profile created successfully with ID: {}", savedProfile.getHealthProfileId());

        // Create assessment via AI server
        try {
            assessmentService.createAssessment(savedProfile);
            log.info("Health profile assessment created successfully");
        } catch (Exception e) {
            log.error("Failed to create assessment, but health profile was saved", e);
            // Don't fail the whole operation if assessment creation fails
        }

        return healthProfileMapper.toDto(savedProfile);
    }

    @Override
    public HealthProfileDto getHealthProfileById(UUID healthProfileId, UUID traineeId) {
        log.info("Fetching health profile ID: {} for trainee ID: {}", healthProfileId, traineeId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> {
                    log.error("Health profile not found with ID: {}", healthProfileId);
                    return new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId);
                });

        // Check if the health profile belongs to the trainee
        if (!healthProfile.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Health profile {} does not belong to trainee {}", healthProfileId, traineeId);
            throw new AccessDeniedException("You do not have permission to access this health profile");
        }

        return healthProfileMapper.toDto(healthProfile);
    }

    @Override
    public List<HealthProfileDto> getAllHealthProfilesByTraineeId(UUID traineeId) {
        log.info("Fetching all health profiles for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        List<HealthProfile> profiles = healthProfileRepository.findByTraineeIdOrderByCreatedAtDesc(traineeId);
        log.info("Found {} health profile(s) for trainee ID: {}", profiles.size(), traineeId);

        return profiles.stream()
                .map(healthProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public HealthProfileDto getLatestHealthProfile(UUID traineeId) {
        log.info("Fetching latest health profile for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        HealthProfile latestProfile = healthProfileRepository.findLatestByTraineeId(traineeId)
                .orElseThrow(() -> {
                    log.error("No health profile found for trainee ID: {}", traineeId);
                    return new HealthProfileNotFoundException("No health profile found for trainee ID: " + traineeId);
                });

        return healthProfileMapper.toDto(latestProfile);
    }

    @Override
    public HealthProfileDto updateHealthProfile(UUID healthProfileId, UUID traineeId, UpdateHealthProfileRequest request) {
        log.info("Updating health profile ID: {} for trainee ID: {}", healthProfileId, traineeId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> {
                    log.error("Health profile not found with ID: {}", healthProfileId);
                    return new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId);
                });

        // Check if the health profile belongs to the trainee
        if (!healthProfile.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Health profile {} does not belong to trainee {}", healthProfileId, traineeId);
            throw new AccessDeniedException("You do not have permission to update this health profile");
        }

        // Delete existing assessment
        try {
            assessmentService.deleteAssessmentByHealthProfileId(healthProfileId);
            log.info("Deleted existing assessment for health profile ID: {}", healthProfileId);
        } catch (Exception e) {
            log.warn("No existing assessment to delete or deletion failed", e);
        }

        healthProfileMapper.updateEntityFromRequest(request, healthProfile);
        HealthProfile updatedProfile = healthProfileRepository.save(healthProfile);
        // Flush to ensure the update is committed before creating new assessment
        healthProfileRepository.flush();
        log.info("Health profile updated successfully with ID: {}", updatedProfile.getHealthProfileId());

        // Create new assessment
        try {
            assessmentService.createAssessment(updatedProfile);
            log.info("New health profile assessment created successfully");
        } catch (Exception e) {
            log.error("Failed to create new assessment after update", e);
        }

        return healthProfileMapper.toDto(updatedProfile);
    }

    @Override
    public void deleteHealthProfile(UUID healthProfileId, UUID traineeId) {
        log.info("Deleting health profile ID: {} for trainee ID: {}", healthProfileId, traineeId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> {
                    log.error("Health profile not found with ID: {}", healthProfileId);
                    return new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId);
                });

        // Check if the health profile belongs to the trainee
        if (!healthProfile.getTrainee().getTraineeId().equals(traineeId)) {
            log.error("Access denied: Health profile {} does not belong to trainee {}", healthProfileId, traineeId);
            throw new AccessDeniedException("You do not have permission to delete this health profile");
        }

        // Delete associated assessment first
        try {
            assessmentService.deleteAssessmentByHealthProfileId(healthProfileId);
            log.info("Deleted assessment for health profile ID: {}", healthProfileId);
        } catch (Exception e) {
            log.warn("No assessment to delete or deletion failed", e);
        }

        healthProfileRepository.delete(healthProfile);
        log.info("Health profile deleted successfully with ID: {}", healthProfileId);
    }

    @Override
    public HealthProfileDto getHealthProfileByIdAdmin(UUID healthProfileId) {
        log.info("Admin fetching health profile with ID: {}", healthProfileId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> {
                    log.error("Health profile not found with ID: {}", healthProfileId);
                    return new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId);
                });

        return healthProfileMapper.toDto(healthProfile);
    }

    @Override
    public List<HealthProfileDto> getAllHealthProfilesByTraineeIdAdmin(UUID traineeId) {
        log.info("Admin fetching all health profiles for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        List<HealthProfile> profiles = healthProfileRepository.findByTraineeIdOrderByCreatedAtDesc(traineeId);
        log.info("Found {} health profile(s) for trainee ID: {}", profiles.size(), traineeId);

        return profiles.stream()
                .map(healthProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<HealthProfileDto> getAllHealthProfilesAdmin() {
        log.info("Admin fetching all health profiles in the system");

        List<HealthProfile> profiles = healthProfileRepository.findAll();
        log.info("Found {} health profile(s) in total", profiles.size());

        return profiles.stream()
                .map(healthProfileMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteHealthProfileAdmin(UUID healthProfileId) {
        log.info("Admin deleting health profile with ID: {}", healthProfileId);

        HealthProfile healthProfile = healthProfileRepository.findById(healthProfileId)
                .orElseThrow(() -> {
                    log.error("Health profile not found with ID: {}", healthProfileId);
                    return new HealthProfileNotFoundException("Health profile not found with ID: " + healthProfileId);
                });

        // Delete associated assessment first
        try {
            assessmentService.deleteAssessmentByHealthProfileId(healthProfileId);
            log.info("Deleted assessment for health profile ID: {}", healthProfileId);
        } catch (Exception e) {
            log.warn("No assessment to delete or deletion failed", e);
        }

        healthProfileRepository.delete(healthProfile);
        log.info("Health profile deleted successfully by admin with ID: {}", healthProfileId);
    }

    @Override
    public HealthProfileWithAssessmentResponse getLatestHealthProfileWithAssessmentAdmin(UUID traineeId) {
        log.info("Admin/Coach fetching latest health profile with assessment for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        // Get latest health profile
        HealthProfile latestProfile = healthProfileRepository.findLatestByTraineeId(traineeId)
                .orElseThrow(() -> {
                    log.error("No health profile found for trainee ID: {}", traineeId);
                    return new HealthProfileNotFoundException("No health profile found for trainee ID: " + traineeId);
                });

        HealthProfileDto profileDto = healthProfileMapper.toDto(latestProfile);

        // Try to get assessment (may not exist)
        HealthProfileAssessmentDto assessmentDto = null;
        try {
            assessmentDto = assessmentService.getAssessmentByHealthProfileId(
                    latestProfile.getHealthProfileId(),
                    traineeId
            );
            log.info("Found assessment for health profile ID: {}", latestProfile.getHealthProfileId());
        } catch (Exception e) {
            log.warn("No assessment found for health profile ID: {}", latestProfile.getHealthProfileId());
        }

        return new HealthProfileWithAssessmentResponse(profileDto, assessmentDto);
    }

    @Override
    public fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse getHealthProfileMetrics(UUID traineeId) {
        log.info("Fetching health profile metrics for trainee ID: {}", traineeId);

        // Verify trainee exists
        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        // Get all health profiles ordered by creation date descending
        List<HealthProfile> profiles = healthProfileRepository.findByTraineeIdOrderByCreatedAtDesc(traineeId);

        if (profiles.isEmpty()) {
            log.error("No health profiles found for trainee ID: {}", traineeId);
            throw new HealthProfileNotFoundException("No health profiles found for trainee ID: " + traineeId);
        }

        // Get latest profile ID
        UUID latestProfileId = profiles.get(0).getHealthProfileId();

        // Convert profiles to metric data points
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> weightKgData = new java.util.ArrayList<>();
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> bmiData = new java.util.ArrayList<>();
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> bodyFatPercentageData = new java.util.ArrayList<>();
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> muscleMassKgData = new java.util.ArrayList<>();
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> waistCmData = new java.util.ArrayList<>();
        List<fpt.edu.sep490.pilahub.dto.response.MetricDataPoint> hipCmData = new java.util.ArrayList<>();

        // Reverse the list to get chronological order (oldest to newest)
        List<HealthProfile> chronologicalProfiles = new java.util.ArrayList<>(profiles);
        java.util.Collections.reverse(chronologicalProfiles);

        for (HealthProfile profile : chronologicalProfiles) {
            java.time.LocalDate date = profile.getCreatedAt()
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate();

            if (profile.getWeightKg() != null) {
                weightKgData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getWeightKg()));
            }
            if (profile.getBmi() != null) {
                bmiData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getBmi()));
            }
            if (profile.getBodyFatPercentage() != null) {
                bodyFatPercentageData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getBodyFatPercentage()));
            }
            if (profile.getMuscleMassKg() != null) {
                muscleMassKgData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getMuscleMassKg()));
            }
            if (profile.getWaistCm() != null) {
                waistCmData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getWaistCm()));
            }
            if (profile.getHipCm() != null) {
                hipCmData.add(new fpt.edu.sep490.pilahub.dto.response.MetricDataPoint(date, profile.getHipCm()));
            }
        }

        fpt.edu.sep490.pilahub.dto.response.HealthMetrics metrics = new fpt.edu.sep490.pilahub.dto.response.HealthMetrics(
                weightKgData,
                bmiData,
                bodyFatPercentageData,
                muscleMassKgData,
                waistCmData,
                hipCmData
        );

        // Calculate comparison between latest and previous (if exists)
        fpt.edu.sep490.pilahub.dto.response.MetricComparison comparison;
        if (profiles.size() >= 2) {
            HealthProfile latest = profiles.get(0);
            HealthProfile previous = profiles.get(1);

            comparison = new fpt.edu.sep490.pilahub.dto.response.MetricComparison(
                    calculateDifference(latest.getWeightKg(), previous.getWeightKg()),
                    calculateDifference(latest.getBmi(), previous.getBmi()),
                    calculateDifference(latest.getBodyFatPercentage(), previous.getBodyFatPercentage()),
                    calculateDifference(latest.getMuscleMassKg(), previous.getMuscleMassKg()),
                    calculateDifference(latest.getWaistCm(), previous.getWaistCm()),
                    calculateDifference(latest.getHipCm(), previous.getHipCm())
            );
        } else {
            // If only one profile, comparison is all zeros or nulls
            comparison = new fpt.edu.sep490.pilahub.dto.response.MetricComparison(
                    null, null, null, null, null, null
            );
        }

        log.info("Successfully retrieved metrics for trainee ID: {} with {} profiles", traineeId, profiles.size());

        return new fpt.edu.sep490.pilahub.dto.response.HealthProfileMetricsResponse(
                traineeId,
                latestProfileId,
                metrics,
                comparison
        );
    }

    @Override
    public HealthProfileDto extractInBodyScan(UUID traineeId, MultipartFile image, String rawScanId) {
        log.info("Creating health profile from InBody scan for trainee ID: {}", traineeId);

        if (!traineeRepository.existsById(traineeId)) {
            log.error("Trainee not found with ID: {}", traineeId);
            throw new TraineeNotFoundException("Trainee not found with ID: " + traineeId);
        }

        validateInBodyImage(image);

        String url = aiServerUrl + aiInBodyExtractEndpoint;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("image", new ByteArrayResource(image.getBytes()) {
                @Override
                public String getFilename() {
                    return image.getOriginalFilename() != null ? image.getOriginalFilename() : "inbody-scan.jpg";
                }
            });

            if (rawScanId != null && !rawScanId.isBlank()) {
                body.add("rawScanId", rawScanId.trim());
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<InBodyExtractApiResponse> response = restTemplate.postForEntity(
                    url,
                    requestEntity,
                    InBodyExtractApiResponse.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null || response.getBody().data() == null) {
                log.error("AI server returned invalid InBody extraction response. Status: {}", response.getStatusCode());
                throw new RuntimeException("Failed to extract InBody metrics from AI server");
            }

            InBodyExtractApiResponse responseBody = response.getBody();
            InBodyExtractData extractedData = responseBody.data();

            CreateHealthProfileRequest createRequest = new CreateHealthProfileRequest(
                    extractedData.heightCm(),
                    extractedData.weightKg(),
                    extractedData.bmi(),
                    extractedData.bodyFatPercentage(),
                    extractedData.muscleMassKg(),
                    extractedData.waistCm(),
                    extractedData.hipCm(),
                    ProfileSource.InBody,
                    extractedData.metadata()
            );

            return createHealthProfile(traineeId, createRequest);
        } catch (IOException e) {
            log.error("Failed to read InBody image for trainee ID: {}", traineeId, e);
            throw new RuntimeException("Failed to read InBody image", e);
        } catch (HttpClientErrorException e) {
            log.warn("AI server rejected InBody extraction request. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new IllegalArgumentException("Invalid InBody scan payload: " + compactErrorMessage(e.getResponseBodyAsString()));
        } catch (HttpServerErrorException e) {
            log.error("AI server failed while extracting InBody scan. Status: {}, Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("AI server failed to extract InBody metrics", e);
        } catch (ResourceAccessException e) {
            log.error("Cannot connect to AI server for InBody extraction: {}", e.getMessage());
            throw new RuntimeException("Cannot connect to AI server for InBody extraction", e);
        }
    }

    private java.math.BigDecimal calculateDifference(java.math.BigDecimal latest, java.math.BigDecimal previous) {
        if (latest == null || previous == null) {
            return null;
        }
        return latest.subtract(previous);
    }

    private void validateInBodyImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("InBody image must not be empty");
        }

        if (image.getSize() > MAX_INBODY_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("InBody image must be smaller than 10MB");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_INBODY_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported image format. Allowed types: image/jpeg, image/png, image/webp");
        }
    }

    private String compactErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return "Please check image format and payload";
        }

        String compact = responseBody.replaceAll("\\s+", " ").trim();
        return compact.length() > 200 ? compact.substring(0, 200) + "..." : compact;
    }
}

package pilahub.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilahub.dto.HealthProfileRequest;
import pilahub.dto.response.HealthProfileAssessmentResponse;
import pilahub.service.GeminiAIService;

@RestController
@RequestMapping("/api/v1/health-assessment")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class HealthAssessmentController {

    private final GeminiAIService geminiAIService;

    @PostMapping("/assess")
    public ResponseEntity<HealthProfileAssessmentResponse> assessHealthProfile(
            @Valid @RequestBody HealthProfileRequest request) {

        log.info("Received health assessment request for age: {}, gender: {}",
                request.getAge(), request.getGender());

        try {
            HealthProfileAssessmentResponse response = geminiAIService.assessHealthProfile(request);

            log.info("Successfully completed health assessment. Score: {}", response.getScore());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing health assessment: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Health Assessment Service is running");
    }
}

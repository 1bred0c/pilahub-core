package pilahub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilahub.dto.request.RoadmapReviewAIRequest;
import pilahub.dto.response.RoadmapReviewAIResponse;
import pilahub.service.RoadmapReviewService;

@RestController
@RequestMapping("/api/v1/roadmap-review")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
@Tag(name = "Roadmap Review", description = "AI-powered roadmap review endpoints")
public class RoadmapReviewController {

    private final RoadmapReviewService roadmapReviewService;

    @PostMapping("/analyze")
    @Operation(summary = "Review roadmap results with AI",
               description = "Analyze roadmap execution results and return structured review JSON")
    public ResponseEntity<RoadmapReviewAIResponse> reviewRoadmap(
            @Valid @RequestBody RoadmapReviewAIRequest request) {

        log.info("Received roadmap review request for roadmapId: {}",
                request.getRoadmap().getRoadmapId());

        try {
            RoadmapReviewAIResponse response = roadmapReviewService.reviewRoadmap(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error processing roadmap review: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if Roadmap Review Service is operational")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Roadmap Review Service is running");
    }
}


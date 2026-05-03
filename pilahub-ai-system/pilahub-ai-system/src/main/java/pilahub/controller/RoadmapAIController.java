package pilahub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pilahub.dto.request.RoadmapAIRequest;
import pilahub.dto.response.RoadmapAIResponse;
import pilahub.exception.ErrorResponse;
import pilahub.service.RoadmapAIService;

@RestController
@RequestMapping("/api/v1/roadmap")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Roadmap AI", description = "AI-powered workout roadmap generation endpoints")
public class RoadmapAIController {

    private final RoadmapAIService roadmapAIService;

    @PostMapping("/generate")
    @Operation(
        summary = "Generate workout roadmap with AI",
        description = "Generate a comprehensive workout roadmap based on user profile, goals, and preferences using Gemini AI. This endpoint may take 30-120 seconds to complete."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Roadmap generated successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = RoadmapAIResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error or AI service failure",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ErrorResponse.class)
            )
        )
    })
    public ResponseEntity<RoadmapAIResponse> generateRoadmap(
            @Valid @RequestBody RoadmapAIRequest request) {
        
        log.info("Received roadmap generation request - Primary Goal: {}, Secondary Goals: {}, Duration: {} weeks, Training days: {}, Level: {}",
                request.primaryGoal(), request.secondaryGoals(), request.durationWeeks(), request.trainingDays().size(), request.workoutLevel());

        long startTime = System.currentTimeMillis();

        try {
            RoadmapAIResponse response = roadmapAIService.generateRoadmap(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("Roadmap generated successfully in {}ms. Stages: {}, Confidence: {}",
                    duration,
                    response.stages() != null ? response.stages().size() : 0,
                    response.confidenceScore());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed to generate roadmap after {}ms: {}", duration, e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/health")
    @Operation(
        summary = "Health check for roadmap AI service",
        description = "Check if the roadmap AI service is operational"
    )
    @ApiResponse(
        responseCode = "200",
        description = "Service is healthy"
    )
    public ResponseEntity<String> healthCheck() {
        log.debug("Health check requested for roadmap AI service");
        return ResponseEntity.ok("Roadmap AI Service is running");
    }
}

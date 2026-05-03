package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.RoadmapReviewDto;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.RoadmapReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/roadmap-reviews")
@RequiredArgsConstructor
@Tag(name = "Roadmap Review", description = "AI-powered roadmap review endpoints")
public class RoadmapReviewController {

    private final RoadmapReviewService roadmapReviewService;

    @PostMapping("/generate/{roadmapId}")
    @Operation(
            summary = "Generate AI review for a roadmap",
            description = "Generate AI-powered review for a roadmap that has 100% progress and both health profiles set"
    )
    @ApiResponse(responseCode = "201", description = "Review generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid roadmap state (progress not 100% or missing health profiles)")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    @ApiResponse(responseCode = "409", description = "Review already exists")
    public ResponseEntity<APIResponse<RoadmapReviewDto>> generateReview(@PathVariable UUID roadmapId) {
        RoadmapReviewDto review = roadmapReviewService.generateReview(roadmapId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Roadmap review generated successfully", review));
    }

    @GetMapping("/roadmap/{roadmapId}")
    @Operation(
            summary = "Get review by roadmap ID",
            description = "Retrieve AI-generated review for a roadmap"
    )
    @ApiResponse(responseCode = "200", description = "Review retrieved successfully")
    @ApiResponse(responseCode = "403", description = "Access denied")
    @ApiResponse(responseCode = "404", description = "Review not found")
    public ResponseEntity<APIResponse<RoadmapReviewDto>> getReviewByRoadmapId(@PathVariable UUID roadmapId) {
        RoadmapReviewDto review = roadmapReviewService.getReviewByRoadmapId(roadmapId);
        return ResponseEntity.ok(APIResponse.success("Roadmap review retrieved successfully", review));
    }
}


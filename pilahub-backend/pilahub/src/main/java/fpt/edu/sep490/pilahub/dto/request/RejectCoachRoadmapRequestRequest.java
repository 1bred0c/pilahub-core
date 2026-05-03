package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for rejecting a coach roadmap request")
public record RejectCoachRoadmapRequestRequest(

        @Schema(description = "Optional note explaining the reason for rejection (max 500 characters)")
        @Size(max = 500, message = "Note must not exceed 500 characters")
        String coachNote
) {
}

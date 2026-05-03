package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Coach rating item for admin dashboard")
public record AdminCoachRatingResponse(
        @Schema(description = "Coach full name", example = "Nguyen Van A") String name,

        @Schema(description = "Coach average rating", example = "4.8") Double avgRating) {
}

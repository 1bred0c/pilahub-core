package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Schema(description = "Coach information")
public record CoachDto(
        @Schema(description = "Unique coach identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID coachId,

        @Schema(description = "Coach's full name", example = "Jane Smith")
        String fullName,

        @Schema(description = "Coach's age", example = "35")
        Integer age,

        @Schema(description = "Coach's gender", example = "FEMALE")
        Gender gender,

        @Schema(description = "Coach's avatar URL", example = "https://example.com/coach-avatar.jpg")
        String avatarUrl,

        @Schema(description = "Coach's biography", example = "Certified Pilates instructor with 10 years of experience")
        String bio,

        @Schema(description = "Years of experience", example = "10")
        Integer yearsOfExperience,

        @Schema(description = "Coach's specialization", example = "Pilates Rehabilitation, Core Strengthening")
        String specialization,

        @Schema(description = "URL to coach's certifications", example = "https://example.com/certifications.pdf")
        String certificationsUrl,

        @Schema(description = "Average rating from feedbacks", example = "4.5")
        Double avgRating,

        @Schema(description = "Price per hour (VND)", example = "500000.00")
        BigDecimal pricePerHour,

        @Schema(description = "Whether the coach is active", example = "true")
        boolean active,

        @Schema(description = "Account creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt
) {
}

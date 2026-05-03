package fpt.edu.sep490.pilahub.dto;

import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.UUID;

@Schema(description = "Personal injury information")
public record PersonalInjuryDto(
        @Schema(description = "Unique personal injury identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID personalInjuryId,

        @Schema(description = "Trainee ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID traineeId,

        @Schema(description = "Injury ID", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID injuryId,

        @Schema(description = "Injury name", example = "Torn ACL")
        String injuryName,

        @Schema(description = "Injury description", example = "Anterior cruciate ligament tear")
        String injuryDescription,

        @Schema(description = "Injury status", example = "ACTIVE")
        InjuryStatus status,

        @Schema(description = "Personal notes about the injury", example = "Got injured during basketball game")
        String notes,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

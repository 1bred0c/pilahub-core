package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Schema(description = "Injury information")
public record InjuryDto(
        @Schema(description = "Unique injury identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID injuryId,

        @Schema(description = "Injury name", example = "Torn ACL")
        String name,

        @Schema(description = "Injury description", example = "Anterior cruciate ligament tear")
        String description,

        @Schema(description = "Symptoms of the injury", example = "Pain, swelling, instability")
        String symptoms,

        @Schema(description = "Common causes", example = "Sudden stops, jumps, or change of direction")
        String causes,

        @Schema(description = "Treatment suggestions", example = "Rest, ice, physical therapy")
        String treatmentSuggestions,

        @Schema(description = "Prevention tips", example = "Proper warm-up, strengthening exercises")
        String preventionTips,

        @Schema(description = "Affected body parts")
        Set<BodyPartDto> affectedBodyParts,

        @Schema(description = "Creation timestamp", example = "2026-01-23T10:30:00Z")
        Instant createdAt,

        @Schema(description = "Last update timestamp", example = "2026-01-23T10:30:00Z")
        Instant updatedAt
) {
}

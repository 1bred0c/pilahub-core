package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Fitness goal information")
public record FitnessGoalDto(

        @Schema(description = "Unique fitness goal identifier", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID goalId,

        @Schema(description = "Fitness goal code", example = "BACK_PAIN_RELIEF")
        String code,

        @Schema(description = "Vietnamese name", example = "Giảm đau lưng")
        String vietnameseName
) {
}

package fpt.edu.sep490.pilahub.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.DayOfWeek;

@Schema(description = "Training day and its preferred start time")
public record TrainingDayScheduleRequest(
        @Schema(description = "Training day of week", example = "MONDAY", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Training day must not be null") DayOfWeek dayOfWeek,

        @Schema(description = "Preferred workout start time in HH:mm", example = "06:30", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank(message = "Start time must not be blank") @Pattern(regexp = "^([01]\\d|2[0-3]):[0-5]\\d$", message = "Start time must be in HH:mm format") String startTime) {
}

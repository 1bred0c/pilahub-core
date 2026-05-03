package fpt.edu.sep490.pilahub.dto.request.roadmap;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Request to update roadmap schedule dates")
public record UpdateRoadmapScheduleRequest(
                @Schema(description = "Start date used to regenerate schedule dates", example = "2026-04-20", requiredMode = Schema.RequiredMode.REQUIRED) @NotNull(message = "Start date must not be null") LocalDate startDate,

                @Schema(description = "Training days of week used for scheduling", example = "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\"]", requiredMode = Schema.RequiredMode.REQUIRED) @NotEmpty(message = "Training days must not be empty") @Size(min = 1, max = 7, message = "Training days must have between 1 and 7 days") List<DayOfWeek> trainingDays) {
}

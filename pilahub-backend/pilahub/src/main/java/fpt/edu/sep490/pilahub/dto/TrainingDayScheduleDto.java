package fpt.edu.sep490.pilahub.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.DayOfWeek;

@Schema(description = "Training day and its start time")
public record TrainingDayScheduleDto(
        @Schema(description = "Training day of week", example = "MONDAY") DayOfWeek dayOfWeek,

        @Schema(description = "Workout start time in HH:mm", example = "06:30") String startTime) {
}

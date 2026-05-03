package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.PersonalScheduleDto;
import fpt.edu.sep490.pilahub.dto.PersonalStageDto;
import fpt.edu.sep490.pilahub.dto.RoadmapDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing roadmap with all nested stages, schedules, and exercises")
public record RoadmapWithDetailsResponse(
        @Schema(description = "Roadmap information")
        RoadmapDto roadmap,

        @Schema(description = "List of personal stages with their schedules and exercises")
        List<StageWithDetails> stages
) {
    @Schema(description = "Personal stage with nested schedules and exercises")
    public record StageWithDetails(
            @Schema(description = "Stage information")
            PersonalStageDto stage,

            @Schema(description = "List of schedules with their exercises")
            List<ScheduleWithDetails> schedules
    ) {}

    @Schema(description = "Personal schedule with nested exercises")
    public record ScheduleWithDetails(
            @Schema(description = "Schedule information")
            PersonalScheduleDto schedule,

            @Schema(description = "List of exercises in this schedule")
            List<PersonalExerciseDto> exercises
    ) {}
}

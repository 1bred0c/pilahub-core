package fpt.edu.sep490.pilahub.dto.response;

import fpt.edu.sep490.pilahub.dto.CourseDto;
import fpt.edu.sep490.pilahub.dto.ExerciseDto;
import fpt.edu.sep490.pilahub.dto.LessonDto;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Response containing course with all nested lessons and exercises")
public record CourseWithDetailsResponse(
        @Schema(description = "Course information")
        CourseDto course,

        @Schema(description = "Ordered list of lessons in this course with their exercises")
        List<LessonWithExercises> lessons
) {
    @Schema(description = "A lesson within the course, with its exercises")
    public record LessonWithExercises(
            @Schema(description = "Course-lesson link identifier")
            UUID courseLessonId,

            @Schema(description = "Display order within the course", example = "1")
            Integer displayOrder,

            @Schema(description = "Notes about this lesson in the course", example = "Warm-up lesson")
            String notes,

            @Schema(description = "Lesson information")
            LessonDto lesson,

            @Schema(description = "Ordered list of exercises in this lesson")
            List<ExerciseInLesson> exercises
    ) {}

    @Schema(description = "An exercise within a lesson, including its prescription details")
    public record ExerciseInLesson(
            @Schema(description = "Lesson-exercise link identifier")
            UUID lessonExerciseId,

            @Schema(description = "Display order within the lesson", example = "1")
            Integer displayOrder,

            @Schema(description = "Number of sets", example = "3")
            Integer sets,

            @Schema(description = "Number of reps", example = "12")
            Integer reps,

            @Schema(description = "Duration in seconds", example = "60")
            Integer durationSeconds,

            @Schema(description = "Rest time in seconds", example = "30")
            Integer restSeconds,

            @Schema(description = "Notes for this exercise", example = "Focus on form")
            String notes,

            @Schema(description = "Exercise information")
            ExerciseDto exercise
    ) {}
}

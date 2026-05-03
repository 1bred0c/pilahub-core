package fpt.edu.sep490.pilahub.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;
import java.util.UUID;

@Schema(description = "Admin edit response containing course with editable lesson/exercise structure")
public record CourseEditDetailsResponse(
        @Schema(description = "Course identifier") UUID courseId,

        @Schema(description = "Course name") String name,

        @Schema(description = "Course description") String description,

        @Schema(description = "Course image URL") String imageUrl,

        @Schema(description = "Difficulty level") String level,

        @Schema(description = "Course price") Double price,

        @Schema(description = "Whether the course is active") boolean active,

        @Schema(description = "Ordered list of lessons in this course for editing") List<LessonForEdit> lessons) {
    @Schema(description = "Lesson node for admin edit")
    public record LessonForEdit(
            @Schema(description = "Course-lesson link identifier") UUID courseLessonId,

            @Schema(description = "Lesson identifier") UUID lessonId,

            @Schema(description = "Display order of lesson in course") Integer displayOrder,

            @Schema(description = "Notes for this lesson within the course") String notes,

            @Schema(description = "Lesson name") String lessonName,

            @Schema(description = "Lesson description") String lessonDescription,

            @Schema(description = "Whether lesson is active") boolean lessonActive,

            @Schema(description = "Ordered exercises inside this lesson for editing") List<ExerciseForEdit> exercises) {
    }

    @Schema(description = "Exercise node for admin edit")
    public record ExerciseForEdit(
            @Schema(description = "Lesson-exercise link identifier") UUID lessonExerciseId,

            @Schema(description = "Exercise identifier") UUID exerciseId,

            @Schema(description = "Display order of exercise in lesson") Integer displayOrder,

            @Schema(description = "Sets") Integer sets,

            @Schema(description = "Reps") Integer reps,

            @Schema(description = "Duration seconds") Integer durationSeconds,

            @Schema(description = "Rest seconds") Integer restSeconds,

            @Schema(description = "Notes") String notes,

            @Schema(description = "Exercise name") String exerciseName,

            @Schema(description = "Exercise description") String exerciseDescription,

            @Schema(description = "Exercise duration in seconds") Integer duration,

            @Schema(description = "Exercise type") String exerciseType,

            @Schema(description = "Difficulty level") String difficultyLevel,

            @Schema(description = "Body parts targeted") List<String> bodyParts,

            @Schema(description = "Whether equipment is required") Boolean equipmentRequired,

            @Schema(description = "Exercise image URL") String imageUrl,

            @Schema(description = "Exercise benefits") String benefits,

            @Schema(description = "Exercise prerequisites") String prerequisites,

            @Schema(description = "Exercise contraindications") String contraindications,

            @Schema(description = "Whether AI support is available") Boolean haveAIsupported,

            @Schema(description = "Name used in AI model") String nameInModelAI,

            @Schema(description = "Breathing rule") String breathingRule,

            @Schema(description = "Whether the exercise is optional in lesson") Boolean optional,

            @Schema(description = "Whether exercise is active") boolean exerciseActive) {
    }
}

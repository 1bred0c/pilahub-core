package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TutorialDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateTutorialRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateTutorialRequest;

import java.util.List;
import java.util.UUID;

public interface TutorialService {

    TutorialDto createTutorial(CreateTutorialRequest request);

    TutorialDto getById(UUID tutorialId);

    TutorialDto getByExerciseId(UUID exerciseId, UUID courseId);

    TutorialDto updateTutorial(UUID tutorialId, UpdateTutorialRequest request);

    void deleteTutorial(UUID tutorialId);

    void deleteByExerciseId(UUID exerciseId);
}

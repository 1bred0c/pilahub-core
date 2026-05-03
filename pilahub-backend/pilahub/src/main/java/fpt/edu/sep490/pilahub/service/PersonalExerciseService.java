package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreatePersonalExerciseRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdatePersonalExerciseRequest;

import java.util.List;
import java.util.UUID;

public interface PersonalExerciseService {

    PersonalExerciseDto createPersonalExercise(CreatePersonalExerciseRequest request);

    PersonalExerciseDto getById(UUID personalExerciseId);

    List<PersonalExerciseDto> getByPersonalScheduleId(UUID personalScheduleId);

    PersonalExerciseDto updatePersonalExercise(UUID personalExerciseId, UpdatePersonalExerciseRequest request);

    PersonalExerciseDto markAsCompleted(UUID personalExerciseId);

    void deletePersonalExercise(UUID personalExerciseId);

    List<PersonalExerciseDto> getCompleted();

    List<PersonalExerciseDto> getIncomplete();
}

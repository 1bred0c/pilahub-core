package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.BodyPartDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateBodyPartRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateBodyPartRequest;

import java.util.List;
import java.util.UUID;

public interface BodyPartService {

    BodyPartDto createBodyPart(CreateBodyPartRequest request);

    List<BodyPartDto> getAll();

    BodyPartDto getById(UUID bodyPartId);

    List<BodyPartDto> searchByName(String name);

    BodyPartDto updateBodyPart(UUID bodyPartId, UpdateBodyPartRequest request);

    void deleteBodyPart(UUID bodyPartId);
}


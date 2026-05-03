package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.EquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.response.EquipmentRoadmapResponse;

import java.util.List;
import java.util.UUID;

public interface EquipmentService {


    EquipmentDto createEquipment(CreateEquipmentRequest request);

    List<EquipmentDto> findAll();

    EquipmentDto getById(UUID equipmentId);

    List<EquipmentDto> searchByName(String name);

    EquipmentDto getByName(String name);

    EquipmentDto updateEquipment(UUID equipmentId, UpdateEquipmentRequest request);

    void deleteEquipment(UUID equipmentId);

    List<EquipmentRoadmapResponse> getEquipmentByRoadmap(UUID roadmapId);
}

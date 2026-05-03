package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PurposeDto;
import fpt.edu.sep490.pilahub.dto.request.purpose.CreatePurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.purpose.UpdatePurposeRequest;

import java.util.List;
import java.util.UUID;

public interface PurposeService {

    PurposeDto createPurpose(CreatePurposeRequest request);

    PurposeDto getById(UUID purposeId);

    PurposeDto getByCode(String code);

    List<PurposeDto> getAll();

    List<PurposeDto> getAllActive();

    List<PurposeDto> searchByName(String name);

    PurposeDto updatePurpose(UUID purposeId, UpdatePurposeRequest request);

    void deactivatePurpose(UUID purposeId);

}

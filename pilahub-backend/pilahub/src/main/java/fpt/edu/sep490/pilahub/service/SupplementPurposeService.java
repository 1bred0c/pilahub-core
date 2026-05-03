package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SupplementPurposeDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementPurposeRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementPurposeRequest;

import java.util.List;
import java.util.UUID;

public interface SupplementPurposeService {

    SupplementPurposeDto createSupplementPurpose(CreateSupplementPurposeRequest request);

    SupplementPurposeDto getById(UUID supplementPurposeId);

    List<SupplementPurposeDto> getBySupplementId(UUID supplementId);

    List<SupplementPurposeDto> getPrimaryPurposesBySupplementId(UUID supplementId);

    List<SupplementPurposeDto> getByPurposeId(UUID purposeId);

    SupplementPurposeDto updateSupplementPurpose(UUID supplementPurposeId, UpdateSupplementPurposeRequest request);

    void deleteSupplementPurpose(UUID supplementPurposeId);

    void deleteBySupplementId(UUID supplementId);

    boolean existsBySupplementAndPurpose(UUID supplementId, UUID purposeId);
}

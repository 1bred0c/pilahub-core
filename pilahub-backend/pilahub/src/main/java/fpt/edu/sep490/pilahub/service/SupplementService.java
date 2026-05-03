package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.SupplementDto;
import fpt.edu.sep490.pilahub.dto.request.supplement.CreateSupplementRequest;
import fpt.edu.sep490.pilahub.dto.request.supplement.UpdateSupplementRequest;

import java.util.List;
import java.util.UUID;

public interface SupplementService {

    SupplementDto createSupplement(CreateSupplementRequest request);

    SupplementDto getById(UUID supplementId);

    List<SupplementDto> getAll();

    List<SupplementDto> getAllActive();

    List<SupplementDto> searchByName(String name);

    List<SupplementDto> getByBrand(String brand);

    List<SupplementDto> getActiveByBrand(String brand);

    SupplementDto updateSupplement(UUID supplementId, UpdateSupplementRequest request);

    void deactivateSupplement(UUID supplementId);

    void deleteSupplement(UUID supplementId);
}

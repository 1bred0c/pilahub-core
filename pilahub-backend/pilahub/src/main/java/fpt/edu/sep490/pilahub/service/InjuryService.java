package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.InjuryDto;

import java.util.List;
import java.util.UUID;

public interface InjuryService {

    InjuryDto getById(UUID injuryId);

    List<InjuryDto> getAllInjuries();

    List<InjuryDto> searchByName(String name);
}

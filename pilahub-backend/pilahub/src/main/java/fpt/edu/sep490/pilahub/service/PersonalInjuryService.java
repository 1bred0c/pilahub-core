package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.PersonalInjuryDto;
import fpt.edu.sep490.pilahub.dto.request.injury.CreatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.dto.request.injury.UpdatePersonalInjuryRequest;
import fpt.edu.sep490.pilahub.enums.InjuryStatus;

import java.util.List;
import java.util.UUID;

public interface PersonalInjuryService {

    PersonalInjuryDto createPersonalInjury(UUID traineeId, CreatePersonalInjuryRequest request);

    PersonalInjuryDto getById(UUID personalInjuryId);

    List<PersonalInjuryDto> getMyInjuries(UUID traineeId);

    List<PersonalInjuryDto> getMyInjuriesByStatus(UUID traineeId, InjuryStatus status);

    PersonalInjuryDto updatePersonalInjury(UUID personalInjuryId, UUID traineeId, UpdatePersonalInjuryRequest request);

    PersonalInjuryDto markAsRecovered(UUID personalInjuryId, UUID traineeId);

    void deletePersonalInjury(UUID personalInjuryId, UUID traineeId);
}

package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.CoachDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CoachService {

    CoachDto createCoach(UUID accountId, CreateCoachRequest request);

    CoachDto getById(UUID coachId);

    List<CoachDto> getAll();

    List<CoachDto> getAllActive();

    List<CoachDto> searchByName(String name);

    CoachDto updateCoach(UUID coachId, UpdateCoachRequest request);

    void activateCoach(UUID coachId);

    void deactivateCoach(UUID coachId);

    void updatePricePerHour(UUID coachId, BigDecimal pricePerHour);
}

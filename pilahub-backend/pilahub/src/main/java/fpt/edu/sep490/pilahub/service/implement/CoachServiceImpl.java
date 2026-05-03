package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.CoachDto;
import fpt.edu.sep490.pilahub.dto.request.coach.CreateCoachRequest;
import fpt.edu.sep490.pilahub.dto.request.coach.UpdateCoachRequest;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.CoachMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Coach;
import fpt.edu.sep490.pilahub.repository.AccountRepository;
import fpt.edu.sep490.pilahub.repository.CoachRepository;
import fpt.edu.sep490.pilahub.service.CoachService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CoachServiceImpl implements CoachService {

    private final CoachRepository coachRepository;
    private final AccountRepository accountRepository;
    private final CoachMapper coachMapper;

    @Override
    public CoachDto createCoach(UUID accountId, CreateCoachRequest request) {
        log.info("Creating coach profile for account ID: {}", accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId));

        if (coachRepository.existsById(accountId)) {
            throw new IllegalStateException("Coach profile already exists for this account");
        }

        Coach coach = coachMapper.toEntity(request);
        coach.setAccount(account);
        coach.setActive(true);
        coach.setPricePerHour(new BigDecimal("500000.00")); // Default 500,000 VND per hour

        Coach saved = coachRepository.save(coach);
        log.info("Successfully created coach profile with ID: {}", saved.getCoachId());

        return coachMapper.toDto(saved);
    }

    @Override
    public CoachDto getById(UUID coachId) {
        log.info("Fetching coach by ID: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        return coachMapper.toDto(coach);
    }

    @Override
    public List<CoachDto> getAll() {
        log.info("Fetching all coaches");

        return coachRepository.findAll().stream()
                .map(coachMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachDto> getAllActive() {
        log.info("Fetching all active coaches");

        return coachRepository.findByActiveTrue().stream()
                .map(coachMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CoachDto> searchByName(String name) {
        log.info("Searching coaches by name: {}", name);

        return coachRepository.findByFullNameContainingIgnoreCase(name).stream()
                .map(coachMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public CoachDto updateCoach(UUID coachId, UpdateCoachRequest request) {
        log.info("Updating coach with ID: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        coachMapper.updateEntityFromRequest(request, coach);

        Coach updated = coachRepository.save(coach);
        log.info("Successfully updated coach with ID: {}", coachId);

        return coachMapper.toDto(updated);
    }

    @Override
    public void activateCoach(UUID coachId) {
        log.info("Activating coach with ID: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        coach.setActive(true);
        coachRepository.save(coach);

        log.info("Successfully activated coach with ID: {}", coachId);
    }

    @Override
    public void deactivateCoach(UUID coachId) {
        log.info("Deactivating coach with ID: {}", coachId);

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        coach.setActive(false);
        coachRepository.save(coach);

        log.info("Successfully deactivated coach with ID: {}", coachId);
    }

    @Override
    public void updatePricePerHour(UUID coachId, BigDecimal pricePerHour) {
        log.info("Updating price per hour for coach with ID: {} to {}", coachId, pricePerHour);

        if (pricePerHour == null || pricePerHour.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price per hour must be greater than 0");
        }

        Coach coach = coachRepository.findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", coachId));

        coach.setPricePerHour(pricePerHour);
        coachRepository.save(coach);

        log.info("Successfully updated price per hour for coach with ID: {}", coachId);
    }
}

package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.InjuryDto;
import fpt.edu.sep490.pilahub.exception.InjuryNotFoundException;
import fpt.edu.sep490.pilahub.mapper.InjuryMapper;
import fpt.edu.sep490.pilahub.pojo.Injury;
import fpt.edu.sep490.pilahub.repository.InjuryRepository;
import fpt.edu.sep490.pilahub.service.InjuryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InjuryServiceImpl implements InjuryService {

    private final InjuryRepository injuryRepository;
    private final InjuryMapper injuryMapper;

    @Override
    public InjuryDto getById(UUID injuryId) {
        log.info("Fetching injury with ID: {}", injuryId);

        Injury injury = injuryRepository.findById(injuryId)
                .orElseThrow(() -> {
                    log.error("Injury not found with ID: {}", injuryId);
                    return new InjuryNotFoundException("Injury not found with ID: " + injuryId);
                });

        return injuryMapper.toDto(injury);
    }

    @Override
    public List<InjuryDto> getAllInjuries() {
        log.info("Fetching all injuries");

        return injuryRepository.findAll().stream()
                .map(injuryMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<InjuryDto> searchByName(String name) {
        log.info("Searching injuries with name containing: {}", name);

        return injuryRepository.findAll().stream()
                .filter(injury -> injury.getName().toLowerCase().contains(name.toLowerCase()))
                .map(injuryMapper::toDto)
                .collect(Collectors.toList());
    }
}

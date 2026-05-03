package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TicketTypeDto;
import fpt.edu.sep490.pilahub.dto.request.tickettype.CreateTicketTypeRequest;
import fpt.edu.sep490.pilahub.dto.request.tickettype.UpdateTicketTypeRequest;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TicketTypeMapper;
import fpt.edu.sep490.pilahub.pojo.TicketType;
import fpt.edu.sep490.pilahub.repository.TicketTypeRepository;
import fpt.edu.sep490.pilahub.service.TicketTypeService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class TicketTypeServiceImpl implements TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final TicketTypeMapper ticketTypeMapper;

    @Override
    public TicketTypeDto createTicketType(CreateTicketTypeRequest request) {
        log.info("Creating ticket type with name: {}", request.name());

        if (ticketTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ticket type with name '" + request.name() + "' already exists");
        }

        TicketType ticketType = ticketTypeMapper.toEntity(request);
        TicketType saved = ticketTypeRepository.save(ticketType);

        log.info("Successfully created ticket type with ID: {}", saved.getTicketTypeId());
        return ticketTypeMapper.toDto(saved);
    }

    @Override
    public TicketTypeDto getById(UUID ticketTypeId) {
        log.info("Fetching ticket type by ID: {}", ticketTypeId);

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        return ticketTypeMapper.toDto(ticketType);
    }

    @Override
    public List<TicketTypeDto> getAll() {
        log.info("Fetching all ticket types");

        return ticketTypeRepository.findAll().stream()
                .map(ticketTypeMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TicketTypeDto updateTicketType(UUID ticketTypeId, UpdateTicketTypeRequest request) {
        log.info("Updating ticket type with ID: {}", ticketTypeId);

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        if (request.name() != null && !request.name().equalsIgnoreCase(ticketType.getName())
                && ticketTypeRepository.existsByNameIgnoreCase(request.name())) {
            throw new DuplicateResourceException("Ticket type with name '" + request.name() + "' already exists");
        }

        ticketTypeMapper.updateEntity(ticketType, request);
        TicketType updated = ticketTypeRepository.save(ticketType);

        log.info("Successfully updated ticket type with ID: {}", ticketTypeId);
        return ticketTypeMapper.toDto(updated);
    }

    @Override
    public void activateTicketType(UUID ticketTypeId) {
        log.info("Activating ticket type with ID: {}", ticketTypeId);

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        ticketType.setActive(true);
        ticketTypeRepository.save(ticketType);

        log.info("Successfully activated ticket type with ID: {}", ticketTypeId);
    }

    @Override
    public void deactivateTicketType(UUID ticketTypeId) {
        log.info("Deactivating ticket type with ID: {}", ticketTypeId);

        TicketType ticketType = ticketTypeRepository.findById(ticketTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", ticketTypeId));

        ticketType.setActive(false);
        ticketTypeRepository.save(ticketType);

        log.info("Successfully deactivated ticket type with ID: {}", ticketTypeId);
    }
}

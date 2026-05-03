package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TicketDto;
import fpt.edu.sep490.pilahub.dto.request.ticket.CreateTicketRequest;
import fpt.edu.sep490.pilahub.dto.request.ticket.UpdateTicketRequest;
import fpt.edu.sep490.pilahub.enums.TicketStatus;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.InvalidRequestException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TicketMapper;
import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.pojo.Ticket;
import fpt.edu.sep490.pilahub.pojo.TicketType;
import fpt.edu.sep490.pilahub.repository.TicketRepository;
import fpt.edu.sep490.pilahub.repository.TicketTypeRepository;
import fpt.edu.sep490.pilahub.service.TicketService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
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
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final TicketMapper ticketMapper;
    private final SecurityUtil securityUtil;

    @Override
    public TicketDto createTicket(CreateTicketRequest request) {
        Account currentUser = securityUtil.getCurrentUser();
        log.info("Creating ticket for account ID: {}", currentUser.getAccountId());

        TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", request.ticketTypeId()));

        if (!ticketType.isActive()) {
            throw new InvalidRequestException("Cannot create ticket with inactive ticket type");
        }

        Ticket ticket = Ticket.builder()
                .account(currentUser)
                .ticketType(ticketType)
                .title(request.title())
                .description(request.description())
                .status(TicketStatus.PENDING)
                .build();

        Ticket saved = ticketRepository.save(ticket);
        log.info("Successfully created ticket with ID: {}", saved.getTicketId());
        return ticketMapper.toDto(saved);
    }

    @Override
    public List<TicketDto> getMyTickets() {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching tickets for account ID: {}", currentUserId);

        return ticketRepository.findByAccount_AccountIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TicketDto getMyTicketById(UUID ticketId) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Fetching ticket {} for account ID: {}", ticketId, currentUserId);

        Ticket ticket = ticketRepository.findByTicketIdAndAccount_AccountId(ticketId, currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        return ticketMapper.toDto(ticket);
    }

    @Override
    public TicketDto updateMyTicket(UUID ticketId, UpdateTicketRequest request) {
        UUID currentUserId = securityUtil.getCurrentUserId();
        log.info("Updating ticket {} for account ID: {}", ticketId, currentUserId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (!ticket.getAccount().getAccountId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only update your own ticket");
        }

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING tickets can be updated");
        }

        if (request.ticketTypeId() != null) {
            TicketType ticketType = ticketTypeRepository.findById(request.ticketTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("TicketType", "id", request.ticketTypeId()));
            if (!ticketType.isActive()) {
                throw new InvalidRequestException("Cannot update ticket to an inactive ticket type");
            }
            ticket.setTicketType(ticketType);
        }

        if (request.title() != null) {
            String title = request.title().trim();
            if (title.isBlank()) {
                throw new InvalidRequestException("Title must not be blank");
            }
            ticket.setTitle(title);
        }

        if (request.description() != null) {
            String description = request.description().trim();
            if (description.isBlank()) {
                throw new InvalidRequestException("Description must not be blank");
            }
            ticket.setDescription(description);
        }

        Ticket updated = ticketRepository.save(ticket);
        log.info("Successfully updated ticket with ID: {}", ticketId);
        return ticketMapper.toDto(updated);
    }

    @Override
    public List<TicketDto> getAllTickets() {
        log.info("Fetching all tickets");

        return ticketRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TicketDto> getTicketsByStatus(TicketStatus status) {
        log.info("Fetching tickets by status: {}", status);

        return ticketRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(ticketMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public TicketDto getTicketById(UUID ticketId) {
        log.info("Fetching ticket by ID: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        return ticketMapper.toDto(ticket);
    }

    @Override
    public TicketDto approveTicket(UUID ticketId) {
        log.info("Approving ticket with ID: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING tickets can be approved");
        }

        ticket.setStatus(TicketStatus.APPROVED);
        Ticket updated = ticketRepository.save(ticket);

        log.info("Successfully approved ticket with ID: {}", ticketId);
        return ticketMapper.toDto(updated);
    }

    @Override
    public TicketDto rejectTicket(UUID ticketId) {
        log.info("Rejecting ticket with ID: {}", ticketId);

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket", "id", ticketId));

        if (ticket.getStatus() != TicketStatus.PENDING) {
            throw new InvalidRequestException("Only PENDING tickets can be rejected");
        }

        ticket.setStatus(TicketStatus.REJECTED);
        Ticket updated = ticketRepository.save(ticket);

        log.info("Successfully rejected ticket with ID: {}", ticketId);
        return ticketMapper.toDto(updated);
    }
}

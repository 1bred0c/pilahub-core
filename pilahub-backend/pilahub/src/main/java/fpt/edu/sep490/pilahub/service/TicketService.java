package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TicketDto;
import fpt.edu.sep490.pilahub.dto.request.ticket.CreateTicketRequest;
import fpt.edu.sep490.pilahub.dto.request.ticket.UpdateTicketRequest;
import fpt.edu.sep490.pilahub.enums.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    TicketDto createTicket(CreateTicketRequest request);

    List<TicketDto> getMyTickets();

    TicketDto getMyTicketById(UUID ticketId);

    TicketDto updateMyTicket(UUID ticketId, UpdateTicketRequest request);

    List<TicketDto> getAllTickets();

    List<TicketDto> getTicketsByStatus(TicketStatus status);

    TicketDto getTicketById(UUID ticketId);

    TicketDto approveTicket(UUID ticketId);

    TicketDto rejectTicket(UUID ticketId);
}

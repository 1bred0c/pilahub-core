package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.TicketTypeDto;
import fpt.edu.sep490.pilahub.dto.request.tickettype.CreateTicketTypeRequest;
import fpt.edu.sep490.pilahub.dto.request.tickettype.UpdateTicketTypeRequest;

import java.util.List;
import java.util.UUID;

public interface TicketTypeService {

    TicketTypeDto createTicketType(CreateTicketTypeRequest request);

    TicketTypeDto getById(UUID ticketTypeId);

    List<TicketTypeDto> getAll();

    TicketTypeDto updateTicketType(UUID ticketTypeId, UpdateTicketTypeRequest request);

    void activateTicketType(UUID ticketTypeId);

    void deactivateTicketType(UUID ticketTypeId);
}

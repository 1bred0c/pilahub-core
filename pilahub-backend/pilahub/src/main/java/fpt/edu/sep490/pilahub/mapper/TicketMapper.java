package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TicketDto;
import fpt.edu.sep490.pilahub.pojo.Ticket;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(target = "accountId", source = "account.accountId")
    @Mapping(target = "ticketTypeId", source = "ticketType.ticketTypeId")
    @Mapping(target = "ticketTypeName", source = "ticketType.name")
    TicketDto toDto(Ticket ticket);
}

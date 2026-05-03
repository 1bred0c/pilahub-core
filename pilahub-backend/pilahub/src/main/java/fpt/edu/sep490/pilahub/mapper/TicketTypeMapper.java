package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TicketTypeDto;
import fpt.edu.sep490.pilahub.dto.request.tickettype.CreateTicketTypeRequest;
import fpt.edu.sep490.pilahub.dto.request.tickettype.UpdateTicketTypeRequest;
import fpt.edu.sep490.pilahub.pojo.TicketType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TicketTypeMapper {

    TicketTypeDto toDto(TicketType ticketType);

    @Mapping(target = "ticketTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    TicketType toEntity(CreateTicketTypeRequest request);

    @Mapping(target = "ticketTypeId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(@MappingTarget TicketType ticketType, UpdateTicketTypeRequest request);
}

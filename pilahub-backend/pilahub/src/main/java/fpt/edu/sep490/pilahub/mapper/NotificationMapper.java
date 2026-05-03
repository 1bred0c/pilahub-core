package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.NotificationDto;
import fpt.edu.sep490.pilahub.pojo.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface NotificationMapper {

    @Mapping(target = "recipientId", source = "recipient.accountId")
    NotificationDto toDto(Notification notification);
}

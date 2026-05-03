package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.MessageDto;
import fpt.edu.sep490.pilahub.dto.MessagePreviewDto;
import fpt.edu.sep490.pilahub.pojo.Messages;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface MessageMapper {

    @Mapping(target = "conversationId", source = "conversation.conversationId")
    @Mapping(target = "senderId", source = "sender.accountId")
    @Mapping(target = "receiverId", source = "receiver.accountId")
    MessageDto toDto(Messages message);

    @Mapping(target = "senderId", source = "sender.accountId")
    @Mapping(target = "receiverId", source = "receiver.accountId")
    MessagePreviewDto toPreviewDto(Messages message);
}


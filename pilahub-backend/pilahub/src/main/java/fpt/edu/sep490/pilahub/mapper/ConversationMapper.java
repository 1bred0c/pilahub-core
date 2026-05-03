package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.ConversationDetailDto;
import fpt.edu.sep490.pilahub.pojo.Conversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = { MessageMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConversationMapper {

    @Mapping(target = "account1Id", source = "account1.accountId")
    @Mapping(target = "account2Id", source = "account2.accountId")
    ConversationDetailDto toDetailDto(Conversation conversation);
}


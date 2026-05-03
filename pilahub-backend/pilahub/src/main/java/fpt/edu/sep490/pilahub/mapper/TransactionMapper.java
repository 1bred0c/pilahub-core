package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.TransactionDto;
import fpt.edu.sep490.pilahub.pojo.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TransactionMapper {

    TransactionDto toDto(Transaction transaction);
}

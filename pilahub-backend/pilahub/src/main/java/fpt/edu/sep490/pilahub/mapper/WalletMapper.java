package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.WalletDto;
import fpt.edu.sep490.pilahub.pojo.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WalletMapper {

    WalletDto toDto(Wallet wallet);
}

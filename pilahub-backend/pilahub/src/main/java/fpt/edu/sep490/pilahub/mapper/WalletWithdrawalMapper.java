package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.WalletWithdrawalDto;
import fpt.edu.sep490.pilahub.pojo.WalletWithdrawal;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface WalletWithdrawalMapper {

    @Mapping(target = "accountId", source = "wallet.accountId")
    WalletWithdrawalDto toDto(WalletWithdrawal walletWithdrawal);
}

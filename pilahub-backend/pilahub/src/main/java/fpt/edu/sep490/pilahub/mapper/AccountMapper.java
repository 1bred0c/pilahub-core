package fpt.edu.sep490.pilahub.mapper;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.enums.PackageType;
import fpt.edu.sep490.pilahub.pojo.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountDto toDto(Account account) {
        return toDto(account, null);
    }

    public AccountDto toDto(Account account, PackageType activePackageType) {
        if (account == null) {
            return null;
        }

        return new AccountDto(
                account.getAccountId(),
                account.getEmail(),
                account.getPhoneNumber(),
                account.getRole(),
                account.isActive(),
                account.isEmailVerified(),
                account.getLastSeenAt(),
                account.getFcmToken(),
                account.getIsReminded(),
                account.getCreatedAt(),
                activePackageType
        );
    }
}

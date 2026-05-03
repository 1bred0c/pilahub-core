package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.enums.Role;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {

    Account registerByEmail(
            String email,
            String phoneNumber,
            String passwordHash,
            Role role);

    Optional<Account> getById(UUID accountId);

    Optional<Account> getActiveByEmail(String email);

    Optional<Account> getActiveByPhoneNumber(String phoneNumber);

    Account updateFcmToken(String fcmToken);

    Account updateIsReminded(Boolean isReminded);

    void verifyEmail(UUID accountId);

    void deactivateAccount(UUID accountId);
}

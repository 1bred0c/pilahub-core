package fpt.edu.sep490.pilahub.service;

import fpt.edu.sep490.pilahub.dto.AccountDto;
import fpt.edu.sep490.pilahub.dto.request.UpdateAccountRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AccountAdminService {

    /**
     * Get all accounts with pagination
     * @param pageable pagination parameters
     * @return paginated list of accounts
     */
    Page<AccountDto> getAllAccounts(Pageable pageable);

    /**
     * Get account by ID
     * @param accountId account ID
     * @return account DTO
     */
    AccountDto getAccountById(UUID accountId);

    /**
     * Update account by ID
     * @param accountId account ID
     * @param request update request
     * @return updated account DTO
     */
    AccountDto updateAccount(UUID accountId, UpdateAccountRequest request);

    /**
     * Delete account by ID (soft delete by setting active to false)
     * @param accountId account ID
     */
    void deleteAccount(UUID accountId);

    /**
     * Activate account by ID
     * @param accountId account ID
     * @return updated account DTO
     */
    AccountDto activateAccount(UUID accountId);
}

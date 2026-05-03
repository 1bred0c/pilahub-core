package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.WalletWithdrawalStatus;
import fpt.edu.sep490.pilahub.pojo.WalletWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletWithdrawalRepository extends JpaRepository<WalletWithdrawal, UUID> {

    List<WalletWithdrawal> findByWallet_AccountIdOrderByRequestedAtDesc(UUID accountId);

    List<WalletWithdrawal> findByStatusOrderByRequestedAtDesc(WalletWithdrawalStatus status);

    List<WalletWithdrawal> findByWallet_AccountIdAndStatusOrderByRequestedAtDesc(UUID accountId, WalletWithdrawalStatus status);

    Optional<WalletWithdrawal> findByWalletWithdrawalIdAndWallet_AccountId(UUID withdrawalId, UUID accountId);

    List<WalletWithdrawal> findAllByOrderByRequestedAtDesc();
}

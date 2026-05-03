package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

    Optional<Wallet> findByAccountId(UUID accountId);

    Optional<Wallet> findByAccount_Email(String email);
}

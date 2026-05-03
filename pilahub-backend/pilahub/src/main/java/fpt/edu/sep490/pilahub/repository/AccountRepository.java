package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Account;
import fpt.edu.sep490.pilahub.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Optional<Account> findByEmailAndActiveTrue(String email);

    Optional<Account> findByPhoneNumberAndActiveTrue(String phoneNumber);

    @Query("SELECT a.accountId FROM Account a WHERE a.active = true")
    List<UUID> findActiveAccountIds();

    boolean existsByRole(Role role);

}

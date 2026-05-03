package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Notification;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    Page<Notification> findByRecipient_AccountIdOrderByCreatedAtDesc(UUID accountId, Pageable pageable);

    long countByRecipient_AccountIdAndReadFalse(UUID accountId);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.recipient.accountId = :accountId AND n.read = false")
    int markAllAsReadByAccountId(@Param("accountId") UUID accountId);
}

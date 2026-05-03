package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.TicketStatus;
import fpt.edu.sep490.pilahub.pojo.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByAccount_AccountIdOrderByCreatedAtDesc(UUID accountId);

    Optional<Ticket> findByTicketIdAndAccount_AccountId(UUID ticketId, UUID accountId);
}

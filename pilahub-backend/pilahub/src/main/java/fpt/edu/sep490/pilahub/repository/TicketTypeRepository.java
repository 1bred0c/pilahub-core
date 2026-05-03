package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, UUID> {

    List<TicketType> findByActiveTrue();

    List<TicketType> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}

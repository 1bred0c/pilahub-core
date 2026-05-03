package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, UUID> {

    List<Equipment> findAll();

    Optional<Equipment> findByName(String name);

    List<Equipment> findByNameContainingIgnoreCase(String name);

    boolean existsByName(String name);

    boolean existsByEquipmentId(UUID equipmentId);
}

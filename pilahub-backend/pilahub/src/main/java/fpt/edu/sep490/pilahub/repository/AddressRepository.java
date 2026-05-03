package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AddressRepository extends JpaRepository<Address, UUID> {

    List<Address> findByTrainee_TraineeId(UUID traineeId);

    @Query("SELECT a FROM Address a WHERE a.trainee.traineeId = :traineeId AND a.isDefault = true")
    Optional<Address> findByTrainee_TraineeIdAndDefaultTrue(@Param("traineeId") UUID traineeId);

    Optional<Address> findByAddressIdAndTrainee_TraineeId(UUID addressId, UUID traineeId);
}

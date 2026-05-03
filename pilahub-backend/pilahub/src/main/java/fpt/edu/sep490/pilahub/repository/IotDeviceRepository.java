package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.IotDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IotDeviceRepository extends JpaRepository<IotDevice, UUID> {

    List<IotDevice> findByTrainee_TraineeId(UUID traineeId);

    Optional<IotDevice> findByIotDeviceIdAndTrainee_TraineeId(UUID iotDeviceId, UUID traineeId);

    boolean existsByDeviceIdentifier(String deviceIdentifier);
}

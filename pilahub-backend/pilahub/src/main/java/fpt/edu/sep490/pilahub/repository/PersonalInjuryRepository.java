package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.enums.InjuryStatus;
import fpt.edu.sep490.pilahub.pojo.PersonalInjury;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PersonalInjuryRepository extends JpaRepository<PersonalInjury, UUID> {

    List<PersonalInjury> findByTraineeTraineeId(UUID traineeId);

    List<PersonalInjury> findByTraineeTraineeIdAndStatus(UUID traineeId, InjuryStatus status);
}

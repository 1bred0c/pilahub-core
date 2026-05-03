package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.SupplementPurpose;
import fpt.edu.sep490.pilahub.pojo.Supplement;
import fpt.edu.sep490.pilahub.pojo.Purpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplementPurposeRepository extends JpaRepository<SupplementPurpose, UUID> {

    List<SupplementPurpose> findBySupplement(Supplement supplement);

    List<SupplementPurpose> findBySupplement_SupplementId(UUID supplementId);

    List<SupplementPurpose> findByPurpose(Purpose purpose);

    List<SupplementPurpose> findByPurpose_PurposeId(UUID purposeId);

    List<SupplementPurpose> findBySupplement_SupplementIdAndPrimaryTrue(UUID supplementId);

    Optional<SupplementPurpose> findBySupplement_SupplementIdAndPurpose_PurposeId(UUID supplementId, UUID purposeId);

    boolean existsBySupplement_SupplementIdAndPurpose_PurposeId(UUID supplementId, UUID purposeId);

    void deleteBySupplement_SupplementId(UUID supplementId);
}

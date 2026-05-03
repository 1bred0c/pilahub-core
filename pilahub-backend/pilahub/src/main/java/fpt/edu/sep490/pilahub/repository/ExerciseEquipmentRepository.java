package fpt.edu.sep490.pilahub.repository;

import fpt.edu.sep490.pilahub.pojo.ExerciseEquipment;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExerciseEquipmentRepository extends JpaRepository<ExerciseEquipment, UUID> {

    List<ExerciseEquipment> findByExercise(Exercise exercise);

    List<ExerciseEquipment> findByExercise_ExerciseId(UUID exerciseId);

    List<ExerciseEquipment> findByEquipment(Equipment equipment);

    List<ExerciseEquipment> findByEquipment_EquipmentId(UUID equipmentId);

    List<ExerciseEquipment> findByExercise_ExerciseIdAndRequiredTrue(UUID exerciseId);

    List<ExerciseEquipment> findByExercise_ExerciseIdAndAlternativeTrue(UUID exerciseId);

    boolean existsByExercise_ExerciseIdAndEquipment_EquipmentId(UUID exerciseId, UUID equipmentId);

    void deleteByExercise_ExerciseId(UUID exerciseId);

    void deleteByEquipment_EquipmentId(UUID equipmentId);
}

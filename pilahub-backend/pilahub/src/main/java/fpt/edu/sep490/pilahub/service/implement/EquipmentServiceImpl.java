package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.EquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.response.EquipmentRoadmapResponse;
import fpt.edu.sep490.pilahub.exception.DuplicateResourceException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.EquipmentMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.EquipmentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentMapper equipmentMapper;
    private final RoadmapRepository roadmapRepository;
    private final PersonalStageRepository personalStageRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final PersonalExerciseRepository personalExerciseRepository;
    private final ExerciseEquipmentRepository exerciseEquipmentRepository;

    @Override
    public EquipmentDto createEquipment(CreateEquipmentRequest request) {
        // Check for duplicate name
        if (equipmentRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Equipment with name '" + request.name() + "' already exists");
        }

        Equipment equipment = equipmentMapper.toEntity(request);
        Equipment saved = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(saved);
    }

    @Override
    public List<EquipmentDto> findAll() {
        List<Equipment> equipments = equipmentRepository.findAll();
        return equipmentMapper.toDto(equipments);
    }


    @Override
    public EquipmentDto getById(UUID equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public List<EquipmentDto> searchByName(String name) {
        return equipmentRepository.findByNameContainingIgnoreCase(name).stream()
                .map(equipmentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EquipmentDto getByName(String name) {
        Equipment equipment = equipmentRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "name", name));
        return equipmentMapper.toDto(equipment);
    }

    @Override
    public EquipmentDto updateEquipment(UUID equipmentId, UpdateEquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", "id", equipmentId));

        // Check for duplicate name if name is being updated
        if (request.name() != null && !request.name().equals(equipment.getName())) {
            if (equipmentRepository.existsByName(request.name())) {
                throw new DuplicateResourceException("Equipment with name '" + request.name() + "' already exists");
            }
        }

        equipmentMapper.updateEntityFromRequest(request, equipment);
        Equipment updated = equipmentRepository.save(equipment);
        return equipmentMapper.toDto(updated);
    }

    @Override
    public void deleteEquipment(UUID equipmentId) {
        if (!equipmentRepository.existsByEquipmentId(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", "id", equipmentId);
        }
        equipmentRepository.deleteById(equipmentId);
    }

    @Override
    public List<EquipmentRoadmapResponse> getEquipmentByRoadmap(UUID roadmapId) {
        // Validate roadmap exists
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        // Get all personal stages for the roadmap
        List<PersonalStage> personalStages = personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap);

        if (personalStages.isEmpty()) {
            log.info("No personal stages found for roadmap {}", roadmapId);
            return new ArrayList<>();
        }

        // Map to track equipment by equipment name
        // Key: Equipment name, Value: EquipmentAggregator (holds aggregation data)
        Map<String, EquipmentAggregator> equipmentMap = new HashMap<>();

        // Collect all exercises from all schedules in all stages
        Set<Exercise> exercises = new HashSet<>();
        for (PersonalStage stage : personalStages) {
            List<PersonalSchedule> schedules = personalScheduleRepository.findByPersonalStage(stage);
            for (PersonalSchedule schedule : schedules) {
                List<PersonalExercise> personalExercises = personalExerciseRepository
                        .findByPersonalScheduleOrderByExerciseOrderAsc(schedule);
                for (PersonalExercise personalExercise : personalExercises) {
                    exercises.add(personalExercise.getExercise());
                }
            }
        }

        log.info("Found {} unique exercises in roadmap {}", exercises.size(), roadmapId);

        // For each exercise, find its equipment
        for (Exercise exercise : exercises) {
            List<ExerciseEquipment> exerciseEquipments = exerciseEquipmentRepository
                    .findByExercise_ExerciseId(exercise.getExerciseId());

            // Process each exercise equipment
            for (ExerciseEquipment exEq : exerciseEquipments) {
                Equipment equipment = exEq.getEquipment();
                String equipmentName = equipment.getName();

                // Get or create aggregator for this equipment
                EquipmentAggregator aggregator = equipmentMap.computeIfAbsent(
                        equipmentName,
                        k -> new EquipmentAggregator(equipment)
                );

                // Update aggregator with this exercise's data
                aggregator.addUsage(exercise.getName(), exEq.isRequired(), exEq.isAlternative(), exEq.getQuantity());
            }
        }

        // Convert aggregators to EquipmentRoadmapResponse
        return equipmentMap.values().stream()
                .map(EquipmentAggregator::toResponse)
                .sorted(Comparator.comparing(EquipmentRoadmapResponse::equipmentName))
                .collect(Collectors.toList());
    }

    /**
     * Helper class to aggregate equipment information across multiple exercises.
     */
    private static class EquipmentAggregator {
        private final Equipment equipment;
        private final Set<String> usedInExercises;
        private boolean hasRequiredUsage; // at least one required=true AND alternative=false
        private boolean hasAlternativeUsage; // at least one alternative=true
        private int maxQuantity;

        public EquipmentAggregator(Equipment equipment) {
            this.equipment = equipment;
            this.usedInExercises = new HashSet<>();
            this.hasRequiredUsage = false;
            this.hasAlternativeUsage = false;
            this.maxQuantity = 0;
        }

        public void addUsage(String exerciseName, boolean required, boolean alternative, Integer quantity) {
            usedInExercises.add(exerciseName);
            
            // Update required status: true if required AND not alternative
            if (required && !alternative) {
                hasRequiredUsage = true;
            }
            
            // Update alternative status
            if (alternative) {
                hasAlternativeUsage = true;
            }
            
            // Update max quantity
            if (quantity != null && quantity > maxQuantity) {
                maxQuantity = quantity;
            }
        }

        public EquipmentRoadmapResponse toResponse() {
            return new EquipmentRoadmapResponse(
                    equipment.getName(),
                    equipment.getDescription(),
                    hasRequiredUsage, // isRequired
                    hasAlternativeUsage, // isAlternative
                    maxQuantity > 0 ? maxQuantity : null,
                    equipment.getImageUrl(),
                    new ArrayList<>(usedInExercises)
            );
        }
    }
}

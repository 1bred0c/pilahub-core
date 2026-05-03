package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.PersonalExerciseDto;
import fpt.edu.sep490.pilahub.dto.RoadmapDto;
import fpt.edu.sep490.pilahub.dto.request.*;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.CreateRoadmapWithDetailsRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapScheduleRequest;
import fpt.edu.sep490.pilahub.dto.request.roadmap.UpdateRoadmapRequest;
import fpt.edu.sep490.pilahub.dto.response.RoadmapAIResponse;
import fpt.edu.sep490.pilahub.dto.response.RoadmapWithDetailsResponse;
import fpt.edu.sep490.pilahub.enums.*;
import fpt.edu.sep490.pilahub.exception.FitnessGoalNotFoundException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.PersonalExerciseMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalScheduleMapper;
import fpt.edu.sep490.pilahub.mapper.PersonalStageMapper;
import fpt.edu.sep490.pilahub.mapper.RoadmapMapper;
import fpt.edu.sep490.pilahub.pojo.*;
import fpt.edu.sep490.pilahub.repository.*;
import fpt.edu.sep490.pilahub.service.RoadmapService;
import fpt.edu.sep490.pilahub.service.SystemConfigService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapMapper roadmapMapper;
    private final PersonalStageRepository personalStageRepository;
    private final PersonalScheduleRepository personalScheduleRepository;
    private final PersonalExerciseRepository personalExerciseRepository;
    private final ExerciseRepository exerciseRepository;
    private final ExerciseEquipmentRepository exerciseEquipmentRepository;
    private final StageRepository stageRepository;
    private final PersonalStageMapper personalStageMapper;
    private final PersonalScheduleMapper personalScheduleMapper;
    private final PersonalExerciseMapper personalExerciseMapper;
    private final TraineeRepository traineeRepository;
    private final HealthProfileRepository healthProfileRepository;
    private final PersonalInjuryRepository personalInjuryRepository;
    private final SupplementRepository supplementRepository;
    private final SupplementIngredientRepository supplementIngredientRepository;
    private final SupplementPurposeRepository supplementPurposeRepository;
    private final IngredientRuleRepository ingredientRuleRepository;
    private final PersonalStageSupplementRepository personalStageSupplementRepository;
    private final RestTemplate restTemplate;
    private final SecurityUtil securityUtil;
    private final SystemConfigService systemConfigService;
    private final CoachRepository coachRepository;
    private final FitnessGoalRepository fitnessGoalRepository;
    private static final ZoneId VN_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    @Value("${ai.server.url:http://localhost:8000}")
    private String aiServerUrl;

    @Value("${ai.server.roadmap-endpoint:/api/v1/roadmap/generate}")
    private String aiRoadmapEndpoint;

    /**
     * Determine roadmap status based on the creator's role.
     * - TRAINEE creates roadmap → READY
     * - COACH creates roadmap → PENDING
     * - Others (ADMIN) → READY
     */
    private RoadmapStatus determineRoadmapStatus() {
        fpt.edu.sep490.pilahub.enums.Role role = securityUtil.getCurrentUser().getRole();
        if (role == fpt.edu.sep490.pilahub.enums.Role.COACH) {
            return RoadmapStatus.PENDING;
        }
        return RoadmapStatus.IN_PROGRESS;
    }

    private UUID resolveLatestHealthProfileId(UUID traineeId) {
        return healthProfileRepository.findLatestByTraineeId(traineeId)
                .map(HealthProfile::getHealthProfileId)
                .orElse(null);
    }

    /**
     * Convert Vietnamese day name to English DayOfWeek enum.
     * 
     * @param dayName the day name in Vietnamese or English
     * @return the corresponding DayOfWeek enum
     */
    private DayOfWeek convertToDayOfWeek(String dayName) {
        if (dayName == null || dayName.trim().isEmpty()) {
            throw new IllegalArgumentException("Day name cannot be null or empty");
        }

        String normalizedDay = dayName.trim().toUpperCase();

        // Try direct English conversion first
        try {
            return DayOfWeek.valueOf(normalizedDay);
        } catch (IllegalArgumentException e) {
            // Not an English day name, try Vietnamese mapping
        }

        // Map Vietnamese day names to English
        return switch (normalizedDay) {
            case "THỨ HAI", "THU HAI", "T2" -> DayOfWeek.MONDAY;
            case "THỨ BA", "THU BA", "T3" -> DayOfWeek.TUESDAY;
            case "THỨ TƯ", "THU TU", "T4" -> DayOfWeek.WEDNESDAY;
            case "THỨ NĂM", "THU NAM", "T5" -> DayOfWeek.THURSDAY;
            case "THỨ SÁU", "THU SAU", "T6" -> DayOfWeek.FRIDAY;
            case "THỨ BẢY", "THU BAY", "T7" -> DayOfWeek.SATURDAY;
            case "CHỦ NHẬT", "CHU NHAT", "CN" -> DayOfWeek.SUNDAY;
            default -> throw new IllegalArgumentException(
                    "Invalid day name: '" + dayName
                            + "'. Must be a valid English day (e.g., MONDAY) or Vietnamese day (e.g., THỨ HAI)");
        };
    }

    @Override
    public RoadmapDto createRoadmap(CreateRoadmapRequest request) {
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();
        fpt.edu.sep490.pilahub.enums.Role currentRole = currentUser.getRole();

        // Determine trainee and coach based on who is creating
        Trainee trainee;
        Coach coach = null;

        if (currentRole == fpt.edu.sep490.pilahub.enums.Role.TRAINEE) {
            // Trainee creating their own roadmap (or optionally for another trainee if ID
            // provided)
            UUID targetTraineeId = request.traineeId() != null ? request.traineeId() : currentUserId;
            trainee = traineeRepository.findById(targetTraineeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", targetTraineeId));
        } else if (currentRole == fpt.edu.sep490.pilahub.enums.Role.COACH) {
            // Coach creating roadmap for a trainee
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Coach must specify traineeId when creating a roadmap");
            }
            trainee = traineeRepository.findById(request.traineeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", request.traineeId()));
            coach = coachRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", currentUserId));
        } else {
            // Admin or other role
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Admin must specify traineeId when creating a roadmap");
            }
            trainee = traineeRepository.findById(request.traineeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", request.traineeId()));
        }

        Roadmap roadmap = Roadmap.builder()
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .source(request.source())
                .progressPercent(0)
                .status(determineRoadmapStatus())
                .trainee(trainee)
                .coach(coach)
                .initialHealthProfileId(resolveLatestHealthProfileId(trainee.getTraineeId()))
                .build();

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Save goals
        saveRoadmapGoals(savedRoadmap, request.primaryGoalId(), request.secondaryGoalIds());

        // Explicitly save to ensure RoadmapGoal entities are persisted
        savedRoadmap = roadmapRepository.save(savedRoadmap);

        return mapRoadmapDtoWithTotalAmount(savedRoadmap);
    }

    @Override
    @Transactional
    public RoadmapWithDetailsResponse createRoadmapWithDetails(CreateRoadmapWithDetailsRequest request) {
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();
        fpt.edu.sep490.pilahub.enums.Role currentRole = currentUser.getRole();

        // Determine trainee and coach based on who is creating
        Trainee trainee;
        Coach coach = null;

        if (currentRole == fpt.edu.sep490.pilahub.enums.Role.TRAINEE) {
            // Trainee creating their own roadmap (or optionally for another trainee if ID
            // provided)
            UUID targetTraineeId = request.traineeId() != null ? request.traineeId() : currentUserId;
            trainee = traineeRepository.findById(targetTraineeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", targetTraineeId));
        } else if (currentRole == fpt.edu.sep490.pilahub.enums.Role.COACH) {
            // Coach creating roadmap for a trainee
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Coach must specify traineeId when creating a roadmap");
            }
            trainee = traineeRepository.findById(request.traineeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", request.traineeId()));
            coach = coachRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", currentUserId));
        } else {
            // Admin or other role
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Admin must specify traineeId when creating a roadmap");
            }
            trainee = traineeRepository.findById(request.traineeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", request.traineeId()));
        }

        // Step 1: Create the Roadmap
        Roadmap roadmap = Roadmap.builder()
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .source(request.source())
                .progressPercent(0)
                .status(determineRoadmapStatus())
                .trainee(trainee)
                .coach(coach)
                .initialHealthProfileId(resolveLatestHealthProfileId(trainee.getTraineeId()))
                .build();

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Save goals
        saveRoadmapGoals(savedRoadmap, request.primaryGoalId(), request.secondaryGoalIds());

        // Explicitly save to ensure RoadmapGoal entities are persisted
        savedRoadmap = roadmapRepository.save(savedRoadmap);

        // List to collect all stages with their nested details
        List<RoadmapWithDetailsResponse.StageWithDetails> stageWithDetailsList = new java.util.ArrayList<>();

        // Step 2: Create PersonalStages if provided
        if (request.stages() != null && !request.stages().isEmpty()) {
            for (CreateRoadmapWithDetailsRequest.PersonalStageDetails stageDetails : request.stages()) {
                // Find or create Stage entity
                Stage stage = stageRepository.findByNameIgnoreCase(stageDetails.stageName())
                        .orElseGet(() -> {
                            Stage newStage = Stage.builder()
                                    .name(stageDetails.stageName())
                                    .description(stageDetails.description())
                                    .active(true)
                                    .build();
                            return stageRepository.save(newStage);
                        });

                PersonalStage personalStage = PersonalStage.builder()
                        .roadmap(savedRoadmap)
                        .stage(stage)
                        .stageName(stage.getName())
                        .stageDescription(stage.getDescription())
                        .stageOrder(stageDetails.stageOrder())
                        .startDate(stageDetails.startDate())
                        .endDate(stageDetails.endDate())
                        .completed(false)
                        .build();

                PersonalStage savedStage = personalStageRepository.save(personalStage);

                // List to collect all schedules for this stage
                List<RoadmapWithDetailsResponse.ScheduleWithDetails> scheduleWithDetailsList = new java.util.ArrayList<>();

                // Step 3: Create PersonalSchedules if provided
                if (stageDetails.schedules() != null && !stageDetails.schedules().isEmpty()) {
                    for (CreateRoadmapWithDetailsRequest.PersonalScheduleDetails scheduleDetails : stageDetails
                            .schedules()) {
                        PersonalSchedule schedule = PersonalSchedule.builder()
                                .personalStage(savedStage)
                                .scheduleName(scheduleDetails.scheduleName())
                                .description(scheduleDetails.description())
                                .scheduledDate(scheduleDetails.scheduledDate())
                                .durationMinutes(scheduleDetails.durationMinutes())
                                .completed(false)
                                .build();

                        PersonalSchedule savedSchedule = personalScheduleRepository.save(schedule);

                        // List to collect all exercises for this schedule
                        List<PersonalExerciseDto> exerciseDtoList = new java.util.ArrayList<>();

                        // Step 4: Create PersonalExercises if provided
                        if (scheduleDetails.exercises() != null && !scheduleDetails.exercises().isEmpty()) {
                            for (CreateRoadmapWithDetailsRequest.PersonalExerciseDetails exerciseDetails : scheduleDetails
                                    .exercises()) {
                                // Validate that the exercise exists
                                Exercise exercise = exerciseRepository.findById(exerciseDetails.exerciseId())
                                        .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id",
                                                exerciseDetails.exerciseId()));

                                PersonalExercise personalExercise = PersonalExercise.builder()
                                        .personalSchedule(savedSchedule)
                                        .exercise(exercise)
                                        .exerciseOrder(exerciseDetails.exerciseOrder())
                                        .sets(exerciseDetails.sets())
                                        .reps(exerciseDetails.reps())
                                        .durationSeconds(exerciseDetails.durationSeconds())
                                        .restSeconds(exerciseDetails.restSeconds())
                                        .notes(exerciseDetails.notes())
                                        .completed(false)
                                        .build();

                                PersonalExercise savedExercise = personalExerciseRepository.save(personalExercise);
                                exerciseDtoList.add(personalExerciseMapper.toDto(savedExercise));
                            }
                        }

                        // Add schedule with its exercises to the list
                        scheduleWithDetailsList.add(new RoadmapWithDetailsResponse.ScheduleWithDetails(
                                personalScheduleMapper.toDto(savedSchedule),
                                exerciseDtoList));
                    }
                }

                // Add stage with its schedules to the list
                stageWithDetailsList.add(new RoadmapWithDetailsResponse.StageWithDetails(
                        personalStageMapper.toDto(savedStage),
                        scheduleWithDetailsList));
            }
        }

        return new RoadmapWithDetailsResponse(
                mapRoadmapDtoWithTotalAmount(savedRoadmap),
                stageWithDetailsList);
    }

    @Override
    @Transactional
    public RoadmapAIResponse createRoadmapWithAI(CreateRoadmapWithAIRequest request) {
        // Determine current user and role
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();
        fpt.edu.sep490.pilahub.enums.Role currentRole = currentUser.getRole();

        // Determine trainee ID based on role and request
        UUID targetTraineeId;

        if (currentRole == fpt.edu.sep490.pilahub.enums.Role.TRAINEE) {
            // Trainee creating their own roadmap (or optionally for another trainee if ID
            // provided)
            targetTraineeId = request.traineeId() != null ? request.traineeId() : currentUserId;
        } else if (currentRole == fpt.edu.sep490.pilahub.enums.Role.COACH) {
            // Coach creating roadmap for a trainee - traineeId is required
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Coach must specify traineeId when creating a roadmap");
            }
            targetTraineeId = request.traineeId();
        } else {
            // Admin or other role - traineeId is required
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Admin must specify traineeId when creating a roadmap");
            }
            targetTraineeId = request.traineeId();
        }

        log.info("Generating AI roadmap suggestion for trainee: {}", targetTraineeId);

        // Step 1: Validate trainee exists
        Trainee trainee = traineeRepository.findById(targetTraineeId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", targetTraineeId));

        // Step 2: Get health profile if exists
        HealthProfile healthProfile = healthProfileRepository.findLatestByTraineeId(targetTraineeId)
                .orElse(null);

        // Step 3: Get personal injuries
        List<PersonalInjury> personalInjuries = personalInjuryRepository.findByTraineeTraineeId(targetTraineeId);

        // Step 4: Build AI request
        RoadmapAIRequest aiRequest = buildRoadmapAIRequest(request, trainee, healthProfile, personalInjuries);

        // Step 5: Call AI server and return the response (not saved yet)
        RoadmapAIResponse aiResponse = callAIServerForRoadmap(aiRequest);

        // Step 6: Enrich the AI response with calculated scheduled dates
        RoadmapAIResponse enrichedResponse = enrichAIResponseWithScheduledDates(aiResponse, request.startDate());

        // Step 7: Enrich the AI response with supplement image URLs
        enrichedResponse = enrichAIResponseWithSupplementImages(enrichedResponse);

        log.info("AI roadmap suggestion generated successfully for trainee: {}", targetTraineeId);
        return enrichedResponse;
    }

    @Override
    @Transactional
    public RoadmapWithDetailsResponse acceptAIGeneratedRoadmap(AcceptAIRoadmapRequest request) {
        // Get current user from token
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();
        fpt.edu.sep490.pilahub.enums.Role currentRole = currentUser.getRole();

        // Determine trainee and coach based on who is creating
        Trainee trainee;
        Coach coach = null;

        if (currentRole == fpt.edu.sep490.pilahub.enums.Role.TRAINEE) {
            // Trainee: use traineeId from request if provided, otherwise use current user
            UUID targetTraineeId = request.traineeId() != null ? request.traineeId() : currentUserId;
            trainee = traineeRepository.findById(targetTraineeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", targetTraineeId));
        } else if (currentRole == fpt.edu.sep490.pilahub.enums.Role.COACH) {
            // Coach: must specify traineeId
            if (request.traineeId() == null) {
                throw new IllegalArgumentException("Coach must specify traineeId when saving roadmap");
            }
            trainee = traineeRepository.findById(request.traineeId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", request.traineeId()));
            coach = coachRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", currentUserId));
        } else {
            throw new IllegalStateException("Only trainees and coaches can save AI-generated roadmaps");
        }

        // Create and save roadmap from AI response
        // Status is determined by determineRoadmapStatus(): IN_PROGRESS for trainee,
        // PENDING for coach
        return createRoadmapFromAIResponse(request.aiResponse(), trainee, coach, request.primaryGoalId(),
                request.secondaryGoalIds());
    }

    private RoadmapAIRequest buildRoadmapAIRequest(CreateRoadmapWithAIRequest request,
            Trainee trainee,
            HealthProfile healthProfile,
            List<PersonalInjury> personalInjuries) {
        // Load primary and secondary goals from database
        fpt.edu.sep490.pilahub.pojo.FitnessGoal primaryGoal = fitnessGoalRepository.findById(request.primaryGoalId())
                .orElseThrow(() -> new FitnessGoalNotFoundException(
                        "Fitness goal not found with ID: " + request.primaryGoalId()));

        List<fpt.edu.sep490.pilahub.pojo.FitnessGoal> secondaryGoals = (request.secondaryGoalIds() != null
                && !request.secondaryGoalIds().isEmpty())
                        ? fitnessGoalRepository.findAllById(request.secondaryGoalIds())
                        : new java.util.ArrayList<>();

        // Convert training days to strings
        List<String> trainingDaysStr = request.trainingDays().stream()
                .map(DayOfWeek::name)
                .collect(Collectors.toList());

        // Convert injuries to AI format
        List<InjuryAIRequest> injuryRequests = personalInjuries.stream()
                .filter(pi -> pi.getStatus() == fpt.edu.sep490.pilahub.enums.InjuryStatus.ACTIVE)
                .map(pi -> new InjuryAIRequest(
                        pi.getInjury().getName(),
                        pi.getInjury().getDescription(),
                        null, // Injury entity has no severity field
                        pi.getInjury().getCauses(),
                        pi.getInjury().getTreatmentSuggestions(),
                        pi.getInjury().getPreventionTips(),
                        pi.getInjury().getAffectedBodyParts().stream()
                                .map(bp -> new fpt.edu.sep490.pilahub.dto.request.AffectedBodyPartAIRequest(
                                        bp.getName(),
                                        bp.getDescription()))
                                .collect(Collectors.toList()),
                        pi.getStatus().name()))
                .collect(Collectors.toList());

        // Determine relevant purposes from primary goal and secondary goals
        Set<String> relevantPurposes = new HashSet<>();
        // Add purposes from primary goal (weighted more heavily in filtering)
        relevantPurposes.addAll(determinePurposesFromGoal(primaryGoal));
        // Add purposes from secondary goals if provided
        if (!secondaryGoals.isEmpty()) {
            for (fpt.edu.sep490.pilahub.pojo.FitnessGoal goal : secondaryGoals) {
                relevantPurposes.addAll(determinePurposesFromGoal(goal));
            }
        }

        // Determine allowed difficulty levels based on workout level
        List<DifficultyLevel> allowedLevels = new ArrayList<>();
        WorkoutLevel workoutLevel = request.workoutLevel();

        if (workoutLevel == WorkoutLevel.BEGINNER) {
            allowedLevels.add(DifficultyLevel.BEGINNER);
        } else if (workoutLevel == WorkoutLevel.INTERMEDIATE) {
            allowedLevels.add(DifficultyLevel.BEGINNER);
            allowedLevels.add(DifficultyLevel.INTERMEDIATE);
        } else if (workoutLevel == WorkoutLevel.ADVANCED) {
            allowedLevels.add(DifficultyLevel.BEGINNER);
            allowedLevels.add(DifficultyLevel.INTERMEDIATE);
            allowedLevels.add(DifficultyLevel.ADVANCED);
        }

        // Fetch active stages
        List<String> availableStages = stageRepository.findAllByActiveTrue().stream()
                .map(Stage::getName)
                .collect(Collectors.toList());

        // Get affected body parts from injuries
        Set<String> affectedBodyParts = personalInjuries.stream()
                .filter(pi -> pi.getStatus() == fpt.edu.sep490.pilahub.enums.InjuryStatus.ACTIVE)
                .flatMap(pi -> pi.getInjury().getAffectedBodyParts().stream())
                .map(BodyPart::getName)
                .collect(Collectors.toSet());

        // Filter exercises based on difficulty level and injury contraindications
        List<ExerciseAIRequest> availableExercises = exerciseRepository.findByActiveTrue().stream()
                .filter(exercise -> {
                    // Filter by difficulty level
                    if (exercise.getDifficultyLevel() != null
                            && !allowedLevels.contains(exercise.getDifficultyLevel())) {
                        return false;
                    }

                    // Filter out exercises targeting injured body parts
                    if (!affectedBodyParts.isEmpty() && exercise.getBodyParts() != null) {
                        boolean targetsInjuredArea = exercise.getBodyParts().stream()
                                .anyMatch(bp -> affectedBodyParts.contains(bp.getName()));
                        if (targetsInjuredArea) {
                            return false;
                        }
                    }

                    // Filter by contraindications if any match trainee's conditions
                    if (exercise.getContraindications() != null && !exercise.getContraindications().isEmpty()) {
                        String contraindications = exercise.getContraindications().toLowerCase();
                        boolean hasContraindication = personalInjuries.stream()
                                .filter(pi -> pi.getStatus() == fpt.edu.sep490.pilahub.enums.InjuryStatus.ACTIVE)
                                .anyMatch(pi -> contraindications.contains(pi.getInjury().getName().toLowerCase()));
                        if (hasContraindication) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(exercise -> new ExerciseAIRequest(
                        exercise.getName(),
                        exercise.getDescription(),
                        exercise.getDuration(),
                        exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null,
                        exercise.getDifficultyLevel() != null ? exercise.getDifficultyLevel().name() : null,
                        exercise.getBodyParts() != null
                                ? exercise.getBodyParts().stream().map(BodyPart::getName).collect(Collectors.toList())
                                : Collections.emptyList(),
                        exercise.isEquipmentRequired(),
                        exercise.getBenefits(),
                        exercise.getPrerequisites(),
                        exercise.getContraindications(),
                        exercise.getNameInModelAI(),
                        exercise.getBreathingRule() != null ? exercise.getBreathingRule().name() : null))
                .collect(Collectors.toList());

        // Build supplement suggestions based on primary goal and trainee profile
        List<SupplementAIRequest> availableSupplements = buildSupplementSuggestions(
                primaryGoal,
                trainee,
                healthProfile,
                personalInjuries);

        // Prepare primary goal description
        String primaryGoalDesc = primaryGoal.getDescription();

        // Prepare secondary goals descriptions
        List<String> secondaryGoalsDesc = null;
        if (!secondaryGoals.isEmpty()) {
            secondaryGoalsDesc = secondaryGoals.stream()
                    .map(fpt.edu.sep490.pilahub.pojo.FitnessGoal::getDescription)
                    .collect(Collectors.toList());
        }

        return new RoadmapAIRequest(
                primaryGoalDesc,
                secondaryGoalsDesc,
                trainee.getAge(),
                trainee.getGender(),
                request.workoutLevel(),
                trainee.getWorkoutFrequency(),
                trainingDaysStr,
                request.durationWeeks(),
                healthProfile != null ? healthProfile.getHeightCm() : null,
                healthProfile != null ? healthProfile.getWeightKg() : null,
                healthProfile != null ? healthProfile.getBmi() : null,
                healthProfile != null ? healthProfile.getBodyFatPercentage() : null,
                healthProfile != null ? healthProfile.getMuscleMassKg() : null,
                healthProfile != null ? healthProfile.getWaistCm() : null,
                healthProfile != null ? healthProfile.getHipCm() : null,
                injuryRequests,
                availableStages,
                availableExercises,
                availableSupplements);
    }

    private List<SupplementAIRequest> buildSupplementSuggestions(fpt.edu.sep490.pilahub.pojo.FitnessGoal goal,
            Trainee trainee,
            HealthProfile healthProfile,
            List<PersonalInjury> personalInjuries) {
        // Determine relevant purposes based on goal
        Set<String> relevantPurposes = determinePurposesFromGoal(goal);

        // Get all active supplements
        List<Supplement> activeSupplements = supplementRepository.findByActiveTrue();

        return activeSupplements.stream()
                .filter(supplement -> {
                    // Filter supplements based on purposes
                    List<SupplementPurpose> purposes = supplementPurposeRepository
                            .findBySupplement_SupplementId(supplement.getSupplementId());
                    boolean hasPrimaryRelevantPurpose = purposes.stream()
                            .filter(SupplementPurpose::isPrimary)
                            .anyMatch(sp -> relevantPurposes.contains(sp.getPurpose().getCode()));

                    if (!hasPrimaryRelevantPurpose && !relevantPurposes.isEmpty()) {
                        return false;
                    }

                    // Filter based on ingredient rules
                    List<SupplementIngredient> supplementIngredients = supplementIngredientRepository
                            .findBySupplement_SupplementId(supplement.getSupplementId());
                    boolean hasBlockingRule = supplementIngredients.stream()
                            .flatMap(si -> ingredientRuleRepository
                                    .findByIngredient_IngredientId(si.getIngredient().getIngredientId()).stream())
                            .anyMatch(rule -> isRuleBlocking(rule, trainee, healthProfile));

                    return !hasBlockingRule;
                })
                .map(supplement -> buildSupplementAIRequest(supplement))
                .collect(Collectors.toList());
    }

    private Set<String> determinePurposesFromGoal(fpt.edu.sep490.pilahub.pojo.FitnessGoal goal) {
        if (goal.getRelatedPurposes() == null || goal.getRelatedPurposes().isEmpty()) {
            return new HashSet<>();
        }
        return goal.getRelatedPurposes().stream()
                .map(fpt.edu.sep490.pilahub.pojo.Purpose::getCode)
                .collect(Collectors.toSet());
    }

    private void saveRoadmapGoals(Roadmap roadmap, UUID primaryGoalId, List<UUID> secondaryGoalIds) {
        // Save primary goal
        fpt.edu.sep490.pilahub.pojo.FitnessGoal primaryGoal = fitnessGoalRepository.findById(primaryGoalId)
                .orElseThrow(() -> new FitnessGoalNotFoundException(
                        "Fitness goal not found with ID: " + primaryGoalId));

        fpt.edu.sep490.pilahub.pojo.RoadmapGoal primaryRoadmapGoal = fpt.edu.sep490.pilahub.pojo.RoadmapGoal.builder()
                .roadmap(roadmap)
                .fitnessGoal(primaryGoal)
                .isPrimary(true)
                .goalOrder(1)
                .build();
        roadmap.getRoadmapGoals().add(primaryRoadmapGoal);

        // Save secondary goals if provided
        if (secondaryGoalIds != null && !secondaryGoalIds.isEmpty()) {
            List<fpt.edu.sep490.pilahub.pojo.FitnessGoal> secondaryGoals = fitnessGoalRepository
                    .findAllById(secondaryGoalIds);

            int order = 2;
            for (fpt.edu.sep490.pilahub.pojo.FitnessGoal secondaryGoal : secondaryGoals) {
                fpt.edu.sep490.pilahub.pojo.RoadmapGoal secondaryRoadmapGoal = fpt.edu.sep490.pilahub.pojo.RoadmapGoal
                        .builder()
                        .roadmap(roadmap)
                        .fitnessGoal(secondaryGoal)
                        .isPrimary(false)
                        .goalOrder(order++)
                        .build();
                roadmap.getRoadmapGoals().add(secondaryRoadmapGoal);
            }
        }
    }

    private boolean isRuleBlocking(IngredientRule rule, Trainee trainee, HealthProfile healthProfile) {
        // Check if rule action is BLOCK
        if (rule.getAction() != fpt.edu.sep490.pilahub.enums.RuleAction.BLOCK) {
            return false;
        }

        // Check different rule types
        switch (rule.getRuleType()) {
            case AGE:
                return checkAgeRule(rule, trainee.getAge());
            case GENDER:
                return checkGenderRule(rule, trainee.getGender());
            case WEIGHT:
                if (healthProfile != null && healthProfile.getWeightKg() != null) {
                    return checkNumericRule(rule, healthProfile.getWeightKg().doubleValue());
                }
                return false;
            case HEIGHT:
                if (healthProfile != null && healthProfile.getHeightCm() != null) {
                    return checkNumericRule(rule, healthProfile.getHeightCm().doubleValue());
                }
                return false;
            default:
                // For other rule types (CONDITION, MEDICATION, ALLERGY), we don't have enough
                // data
                // So we'll be conservative and not block
                return false;
        }
    }

    private boolean checkAgeRule(IngredientRule rule, Integer age) {
        if (rule.getValue() == null || age == null) {
            return false;
        }

        try {
            double ruleValue = Double.parseDouble(rule.getValue());
            switch (rule.getOperator()) {
                case LESS_THAN:
                    return age < ruleValue;
                case LESS_THAN_OR_EQUAL:
                    return age <= ruleValue;
                case GREATER_THAN:
                    return age > ruleValue;
                case GREATER_THAN_OR_EQUAL:
                    return age >= ruleValue;
                case EQUALS:
                    return age == ruleValue;
                case NOT_EQUALS:
                    return age != ruleValue;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean checkGenderRule(IngredientRule rule, fpt.edu.sep490.pilahub.enums.Gender gender) {
        if (rule.getValue() == null || gender == null) {
            return false;
        }

        return rule.getOperator() == fpt.edu.sep490.pilahub.enums.RuleOperator.EQUALS
                && rule.getValue().equalsIgnoreCase(gender.name());
    }

    private boolean checkNumericRule(IngredientRule rule, double value) {
        if (rule.getValue() == null) {
            return false;
        }

        try {
            double ruleValue = Double.parseDouble(rule.getValue());
            switch (rule.getOperator()) {
                case LESS_THAN:
                    return value < ruleValue;
                case LESS_THAN_OR_EQUAL:
                    return value <= ruleValue;
                case GREATER_THAN:
                    return value > ruleValue;
                case GREATER_THAN_OR_EQUAL:
                    return value >= ruleValue;
                case EQUALS:
                    return Math.abs(value - ruleValue) < 0.01;
                case NOT_EQUALS:
                    return Math.abs(value - ruleValue) >= 0.01;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private SupplementAIRequest buildSupplementAIRequest(Supplement supplement) {
        // Get ingredients for this supplement
        List<SupplementIngredient> supplementIngredients = supplementIngredientRepository
                .findBySupplement_SupplementId(supplement.getSupplementId());

        List<IngredientAIRequest> ingredients = supplementIngredients.stream()
                .map(si -> {
                    // Get rules for this ingredient
                    List<IngredientRule> rules = ingredientRuleRepository
                            .findByIngredient_IngredientId(si.getIngredient().getIngredientId());

                    List<IngredientRuleAIRequest> ruleRequests = rules.stream()
                            .map(rule -> new IngredientRuleAIRequest(
                                    rule.getRuleType().name(),
                                    rule.getRuleDescription(),
                                    rule.getOperator().name(),
                                    rule.getValue(),
                                    rule.getSeverity().name(),
                                    rule.getAction().name()))
                            .collect(Collectors.toList());

                    return new IngredientAIRequest(
                            si.getIngredient().getName(),
                            si.getAmount(),
                            si.getUnit(),
                            ruleRequests);
                })
                .collect(Collectors.toList());

        // Get purposes for this supplement
        List<SupplementPurpose> supplementPurposes = supplementPurposeRepository
                .findBySupplement_SupplementId(supplement.getSupplementId());

        List<String> purposes = supplementPurposes.stream()
                .filter(SupplementPurpose::isPrimary)
                .map(sp -> sp.getPurpose().getName())
                .collect(Collectors.toList());

        return new SupplementAIRequest(
                supplement.getName(),
                supplement.getDescription(),
                supplement.getBrand(),
                supplement.getForm(),
                supplement.getUsageInstructions(),
                supplement.getBenefits(),
                supplement.getSideEffects(),
                supplement.getContraindications(),
                supplement.getWarnings(),
                ingredients,
                purposes);
    }

    private RoadmapAIResponse callAIServerForRoadmap(RoadmapAIRequest request) {
        String url = aiServerUrl + aiRoadmapEndpoint;
        log.info("Calling AI server for roadmap generation at: {} (this may take up to 2 minutes)", url);

        long startTime = System.currentTimeMillis();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<RoadmapAIRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<RoadmapAIResponse> response = restTemplate.postForEntity(
                    url,
                    entity,
                    RoadmapAIResponse.class);

            long duration = System.currentTimeMillis() - startTime;

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error("AI server returned non-successful status: {} after {}ms", response.getStatusCode(),
                        duration);
                throw new RuntimeException(
                        "Failed to generate roadmap from AI server. Status: " + response.getStatusCode());
            }

            log.info("Successfully received AI roadmap response after {}ms", duration);
            return response.getBody();

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Error calling AI server after {}ms: {}", duration, e.getMessage(), e);
            throw new RuntimeException("Failed to generate roadmap from AI server: " + e.getMessage(), e);
        }
    }

    /**
     * Enrich the AI response with calculated scheduled dates for each schedule
     * based on stage start dates and day of week
     */
    private RoadmapAIResponse enrichAIResponseWithScheduledDates(RoadmapAIResponse aiResponse, LocalDate startDate) {
        if (aiResponse == null || aiResponse.stages() == null) {
            return aiResponse;
        }

        LocalDate roadmapStartDate = startDate != null
                ? startDate
                : LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        LocalDate currentStageStartDate = roadmapStartDate;

        List<RoadmapAIResponse.StageAIResponse> enrichedStages = new ArrayList<>();

        Map<String, Exercise> exerciseMap = exerciseRepository.findAll()
                .stream()
                .collect(Collectors.toMap(
                        e -> e.getName().trim().toLowerCase(),
                        Function.identity(),
                        (a, b) -> a));

        for (RoadmapAIResponse.StageAIResponse stageAI : aiResponse.stages()) {

            LocalDate stageStartDate = currentStageStartDate;
            LocalDate stageEndDate = stageStartDate.plusWeeks(stageAI.durationWeeks()).minusDays(1);

            List<RoadmapAIResponse.ScheduleAIResponse> expandedSchedules = new ArrayList<>();

            if (stageAI.schedules() != null && !stageAI.schedules().isEmpty()) {

                LocalDate cursorDate = stageStartDate;

                while (!cursorDate.isAfter(stageEndDate)) {
                    DayOfWeek currentDay = cursorDate.getDayOfWeek();

                    for (RoadmapAIResponse.ScheduleAIResponse scheduleTemplate : stageAI.schedules()) {
                        DayOfWeek templateDay = convertToDayOfWeek(scheduleTemplate.dayOfWeek());

                        if (currentDay == templateDay) {
                            Instant scheduledDate = cursorDate
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant();

                            List<RoadmapAIResponse.ExerciseAIResponse> enrichedExercises = new ArrayList<>();

                            if (scheduleTemplate.exercises() != null) {
                                for (RoadmapAIResponse.ExerciseAIResponse aiExercise : scheduleTemplate.exercises()) {

                                    Exercise matchedExercise = exerciseMap.get(
                                            aiExercise.exerciseName().trim().toLowerCase());

                                    RoadmapAIResponse.ExerciseAIResponse enrichedExercise = new RoadmapAIResponse.ExerciseAIResponse(
                                            aiExercise.exerciseName(),
                                            matchedExercise != null ? matchedExercise.getExerciseId() : null,
                                            matchedExercise != null ? matchedExercise.getImageUrl() : null,
                                            aiExercise.exerciseOrder(),
                                            aiExercise.sets(),
                                            aiExercise.reps(),
                                            aiExercise.durationSeconds(),
                                            aiExercise.restSeconds(),
                                            aiExercise.intensity(),
                                            aiExercise.notes());

                                    enrichedExercises.add(enrichedExercise);
                                }
                            }

                            RoadmapAIResponse.ScheduleAIResponse expandedSchedule = new RoadmapAIResponse.ScheduleAIResponse(
                                    scheduleTemplate.scheduleName(),
                                    scheduleTemplate.description(),
                                    scheduleTemplate.dayOfWeek(),
                                    scheduledDate,
                                    scheduleTemplate.durationMinutes(),
                                    enrichedExercises,
                                    scheduleTemplate.notes());

                            expandedSchedules.add(expandedSchedule);
                        }
                    }

                    cursorDate = cursorDate.plusDays(1);
                }
            }

            // Sort chronologically just in case
            expandedSchedules.sort(Comparator.comparing(RoadmapAIResponse.ScheduleAIResponse::scheduledDate));

            RoadmapAIResponse.StageAIResponse enrichedStage = new RoadmapAIResponse.StageAIResponse(
                    stageAI.stageName(),
                    stageAI.description(),
                    stageAI.stageOrder(),
                    stageAI.durationWeeks(),
                    expandedSchedules,
                    stageAI.supplementRecommendations(),
                    stageAI.notes());

            enrichedStages.add(enrichedStage);

            // next stage starts immediately after this stage ends
            currentStageStartDate = stageEndDate.plusDays(1);
        }

        return new RoadmapAIResponse(
                aiResponse.title(),
                aiResponse.description(),
                enrichedStages,
                aiResponse.confidenceScore(),
                aiResponse.aiModel(),
                aiResponse.generatedAt(),
                aiResponse.notes());
    }

    /**
     * Enrich the AI response with supplement image URLs from the database
     */
    private RoadmapAIResponse enrichAIResponseWithSupplementImages(RoadmapAIResponse aiResponse) {
        if (aiResponse == null || aiResponse.stages() == null) {
            return aiResponse;
        }

        List<RoadmapAIResponse.StageAIResponse> enrichedStages = new ArrayList<>();

        for (RoadmapAIResponse.StageAIResponse stageAI : aiResponse.stages()) {
            List<fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse> enrichedSupplements = new ArrayList<>();

            // Enrich supplement recommendations with image URLs
            if (stageAI.supplementRecommendations() != null) {
                for (fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse supplementRec : stageAI
                        .supplementRecommendations()) {
                    String imageUrl = null;

                    // Look up the supplement in the database by name
                    Optional<Supplement> supplementOpt = supplementRepository
                            .findByName(supplementRec.supplementName());
                    if (supplementOpt.isPresent()) {
                        imageUrl = supplementOpt.get().getImageUrl();
                        log.debug("Found image URL for supplement '{}': {}", supplementRec.supplementName(), imageUrl);
                    } else {
                        log.warn("Supplement '{}' not found in database, image URL will be null",
                                supplementRec.supplementName());
                    }

                    // Create new supplement recommendation with image URL
                    fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse enrichedSupplement = new fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse(
                            supplementRec.supplementName(),
                            supplementRec.recommendedTiming(),
                            supplementRec.dosage(),
                            supplementRec.reason(),
                            supplementRec.priority(),
                            imageUrl);

                    enrichedSupplements.add(enrichedSupplement);
                }
            }

            // Create new stage with enriched supplements
            RoadmapAIResponse.StageAIResponse enrichedStage = new RoadmapAIResponse.StageAIResponse(
                    stageAI.stageName(),
                    stageAI.description(),
                    stageAI.stageOrder(),
                    stageAI.durationWeeks(),
                    stageAI.schedules(),
                    enrichedSupplements.isEmpty() ? stageAI.supplementRecommendations() : enrichedSupplements,
                    stageAI.notes());

            enrichedStages.add(enrichedStage);
        }

        // Return new enriched response
        return new RoadmapAIResponse(
                aiResponse.title(),
                aiResponse.description(),
                enrichedStages,
                aiResponse.confidenceScore(),
                aiResponse.aiModel(),
                aiResponse.generatedAt(),
                aiResponse.notes());
    }

    private RoadmapWithDetailsResponse createRoadmapFromAIResponse(RoadmapAIResponse aiResponse,
            Trainee trainee,
            Coach coach,
            UUID primaryGoalId,
            List<UUID> secondaryGoalIds) {
        // Create roadmap
        LocalDate roadmapStartLocalDate = resolveRoadmapStartDate(aiResponse);

        Roadmap roadmap = Roadmap.builder()
                .title(aiResponse.title())
                .description(aiResponse.description())
                .startDate(roadmapStartLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                .progressPercent(0)
                .source("AI_GENERATED")
                .status(determineRoadmapStatus())
                .trainee(trainee)
                .coach(coach)
                .initialHealthProfileId(resolveLatestHealthProfileId(trainee.getTraineeId()))
                .build();

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        // Save goals
        saveRoadmapGoals(savedRoadmap, primaryGoalId, secondaryGoalIds);

        // Explicitly save to ensure RoadmapGoal entities are persisted
        savedRoadmap = roadmapRepository.save(savedRoadmap);

        List<RoadmapWithDetailsResponse.StageWithDetails> stageWithDetailsList = new ArrayList<>();

        LocalDate currentStageStartDate = roadmapStartLocalDate;

        // Process stages from AI response
        if (aiResponse.stages() != null) {
            // int stageWeekOffset = 0;

            for (RoadmapAIResponse.StageAIResponse stageAI : aiResponse.stages()) {
                // LocalDate defaultStageStartDate = currentDate.plusWeeks(stageWeekOffset);
                LocalDate defaultStageStartDate = currentStageStartDate;
                LocalDate stageStartDate = resolveStageStartDate(stageAI, defaultStageStartDate);
                LocalDate stageEndDate = stageStartDate.plusWeeks(stageAI.durationWeeks()).minusDays(1);

                // Find or create Stage entity based on AI response
                Stage stage = stageRepository.findByNameIgnoreCase(stageAI.stageName())
                        .orElseGet(() -> {
                            Stage newStage = Stage.builder()
                                    .name(stageAI.stageName())
                                    .description(stageAI.description())
                                    .active(true)
                                    .build();
                            return stageRepository.save(newStage);
                        });

                PersonalStage personalStage = PersonalStage.builder()
                        .roadmap(savedRoadmap)
                        .stage(stage)
                        .stageName(stageAI.stageName())
                        .stageDescription(stageAI.description())
                        .stageOrder(stageAI.stageOrder())
                        .durationWeeks(stageAI.durationWeeks())
                        .startDate(stageStartDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        .endDate(stageEndDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
                        .completed(false)
                        .build();

                PersonalStage savedStage = personalStageRepository.save(personalStage);

                List<RoadmapWithDetailsResponse.ScheduleWithDetails> scheduleWithDetailsList = new ArrayList<>();

                // Process schedules for this stage - create schedules for ALL weeks in the
                // stage
                if (stageAI.schedules() != null) {
                    // For each week in the stage duration
                    // for (int week = 0; week < stageAI.durationWeeks(); week++) {
                    // For each schedule pattern in the AI response
                    for (RoadmapAIResponse.ScheduleAIResponse scheduleAI : stageAI.schedules()) {
                        String dayOfWeek = scheduleAI.dayOfWeek();

                        // Use pre-calculated scheduled date if available, otherwise calculate it
                        Instant scheduledDateInstant;

                        if (scheduleAI.scheduledDate() != null) {
                            scheduledDateInstant = scheduleAI.scheduledDate();
                        } else {
                            DayOfWeek dayOfWeekEnum = convertToDayOfWeek(scheduleAI.dayOfWeek());
                            LocalDate scheduleDate = stageStartDate
                                    .with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeekEnum));
                            scheduledDateInstant = scheduleDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
                        }

                        // Make sure the schedule date is within the stage bounds
                        LocalDate scheduleDate = LocalDate.ofInstant(scheduledDateInstant, ZoneId.systemDefault());
                        if (scheduleDate.isAfter(stageEndDate)
                                || scheduleDate.isAfter(LocalDate.now().plusYears(2))) {
                            continue; // Skip schedules that fall outside the stage or too far in future
                        }

                        PersonalSchedule schedule = PersonalSchedule.builder()
                                .personalStage(savedStage)
                                .scheduleName(scheduleAI.scheduleName())
                                .description(scheduleAI.description())
                                .dayOfWeek(dayOfWeek)
                                .scheduledDate(scheduledDateInstant)
                                .durationMinutes(scheduleAI.durationMinutes())
                                .completed(false)
                                .build();

                        PersonalSchedule savedSchedule = personalScheduleRepository.save(schedule);

                        List<PersonalExerciseDto> exerciseDtoList = new ArrayList<>();

                        // Process exercises for this schedule
                        if (scheduleAI.exercises() != null) {
                            for (RoadmapAIResponse.ExerciseAIResponse exerciseAI : scheduleAI.exercises()) {
                                // Find exercise by name - do not create if it doesn't exist
                                Optional<Exercise> exerciseOpt = exerciseRepository
                                        .findByNameIgnoreCase(exerciseAI.exerciseName());

                                if (exerciseOpt.isEmpty()) {
                                    log.warn("Exercise '{}' not found in database for schedule {}. Skipping.",
                                            exerciseAI.exerciseName(), savedSchedule.getPersonalScheduleId());
                                    continue;
                                }

                                Exercise exercise = exerciseOpt.get();

                                PersonalExercise personalExercise = PersonalExercise.builder()
                                        .personalSchedule(savedSchedule)
                                        .exercise(exercise)
                                        .exerciseOrder(exerciseAI.exerciseOrder())
                                        .sets((exerciseAI.sets() == null || exerciseAI.sets() <= 0) ? 1
                                                : exerciseAI.sets())
                                        .reps((exerciseAI.reps() == null || exerciseAI.reps() <= 0) ? 1
                                                : exerciseAI.reps())
                                        .durationSeconds((exerciseAI.durationSeconds() == null
                                                || exerciseAI.durationSeconds() <= 0) ? 30
                                                        : exerciseAI.durationSeconds())
                                        .restSeconds((exerciseAI.restSeconds() == null
                                                || exerciseAI.restSeconds() <= 0) ? 30
                                                        : exerciseAI.restSeconds())
                                        .notes(exerciseAI.notes())
                                        .completed(false)
                                        .build();

                                PersonalExercise savedExercise = personalExerciseRepository.save(personalExercise);
                                exerciseDtoList.add(personalExerciseMapper.toDto(savedExercise));
                            }
                        }

                        log.info("Saving schedule date: {}", scheduledDateInstant);

                        scheduleWithDetailsList.add(new RoadmapWithDetailsResponse.ScheduleWithDetails(
                                personalScheduleMapper.toDto(savedSchedule),
                                exerciseDtoList));
                    }

                }

                // Process supplement recommendations for this stage
                if (stageAI.supplementRecommendations() != null && !stageAI.supplementRecommendations().isEmpty()) {
                    processSupplementRecommendationsForStage(stageAI.supplementRecommendations(), savedStage);
                }

                log.info("Stage {} schedules from AI after enrich: {}", stageAI.stageName(),
                        stageAI.schedules().size());

                stageWithDetailsList.add(new RoadmapWithDetailsResponse.StageWithDetails(
                        personalStageMapper.toDto(savedStage),
                        scheduleWithDetailsList));

                currentStageStartDate = stageEndDate.plusDays(1);
            }
        }

        // Set end date for roadmap
        if (!stageWithDetailsList.isEmpty()) {
            RoadmapWithDetailsResponse.StageWithDetails lastStage = stageWithDetailsList
                    .get(stageWithDetailsList.size() - 1);
            savedRoadmap.setEndDate(lastStage.stage().endDate());
            roadmapRepository.save(savedRoadmap);
        }

        return new RoadmapWithDetailsResponse(
                mapRoadmapDtoWithTotalAmount(savedRoadmap),
                stageWithDetailsList);
    }

    private LocalDate resolveRoadmapStartDate(RoadmapAIResponse aiResponse) {
        if (aiResponse == null || aiResponse.stages() == null) {
            return LocalDate.now(VN_ZONE);
        }

        return aiResponse.stages().stream()
                .filter(Objects::nonNull)
                .flatMap(stage -> stage.schedules() == null ? java.util.stream.Stream.empty()
                        : stage.schedules().stream())
                .filter(Objects::nonNull)
                .map(RoadmapAIResponse.ScheduleAIResponse::scheduledDate)
                .filter(Objects::nonNull)
                .map(date -> LocalDate.ofInstant(date, ZoneId.systemDefault()))
                .min(LocalDate::compareTo)
                .orElse(LocalDate.now(VN_ZONE));
    }

    private LocalDate resolveStageStartDate(RoadmapAIResponse.StageAIResponse stageAI, LocalDate defaultStartDate) {
        if (stageAI == null || stageAI.schedules() == null) {
            return defaultStartDate;
        }

        return stageAI.schedules().stream()
                .filter(Objects::nonNull)
                .map(RoadmapAIResponse.ScheduleAIResponse::scheduledDate)
                .filter(Objects::nonNull)
                .map(date -> LocalDate.ofInstant(date, ZoneId.systemDefault()))
                .min(LocalDate::compareTo)
                .orElse(defaultStartDate);
    }

    private void processSupplementRecommendationsForStage(
            List<fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse> supplementRecommendations,
            PersonalStage personalStage) {

        for (fpt.edu.sep490.pilahub.dto.response.SupplementRecommendationAIResponse supplementRec : supplementRecommendations) {
            // Find supplement by name - do not create if it doesn't exist
            Optional<Supplement> supplementOpt = supplementRepository.findByName(supplementRec.supplementName());

            if (supplementOpt.isEmpty()) {
                log.warn("Supplement '{}' not found in database for stage {}. Skipping.",
                        supplementRec.supplementName(), personalStage.getStageOrder());
                continue;
            }

            Supplement supplement = supplementOpt.get();

            // Check if this supplement is already assigned to this stage
            if (personalStageSupplementRepository.existsByPersonalStage_PersonalStageIdAndSupplement_SupplementId(
                    personalStage.getPersonalStageId(), supplement.getSupplementId())) {
                log.debug("Supplement {} already assigned to stage {}. Skipping.",
                        supplement.getName(), personalStage.getStageOrder());
                continue;
            }

            // Create PersonalStageSupplement
            PersonalStageSupplement personalStageSupplement = PersonalStageSupplement.builder()
                    .personalStage(personalStage)
                    .supplement(supplement)
                    .recommendedTiming(supplementRec.recommendedTiming())
                    .dosage(supplementRec.dosage())
                    .reason(supplementRec.reason())
                    .priority(supplementRec.priority())
                    .optional(false) // AI recommendations are typically not optional
                    .build();

            personalStageSupplementRepository.save(personalStageSupplement);
            log.info("Created supplement assignment: {} for stage {} (Order: {})",
                    supplement.getName(), personalStage.getPersonalStageId(), personalStage.getStageOrder());
        }
    }

    @Override
    public RoadmapDto getById(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));
        return mapRoadmapDtoWithTotalAmount(roadmap);
    }

    @Override
    public RoadmapWithDetailsResponse getRoadmapWithDetails(UUID roadmapId) {
        // Fetch the roadmap
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        // Fetch all stages for this roadmap (ordered by stageOrder)
        List<PersonalStage> stages = personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap);

        // Build the response with nested details
        List<RoadmapWithDetailsResponse.StageWithDetails> stageWithDetailsList = stages.stream()
                .map(stage -> {
                    // Fetch all schedules for this stage (ordered by scheduledDate)
                    List<PersonalSchedule> schedules = personalScheduleRepository
                            .findByPersonalStageOrderByScheduledDateAsc(stage);

                    // Build schedule details with exercises
                    List<RoadmapWithDetailsResponse.ScheduleWithDetails> scheduleWithDetailsList = schedules.stream()
                            .map(schedule -> {
                                // Fetch all exercises for this schedule (ordered by exerciseOrder)
                                List<PersonalExercise> exercises = personalExerciseRepository
                                        .findByPersonalScheduleOrderByExerciseOrderAsc(schedule);

                                // Convert exercises to DTOs
                                List<fpt.edu.sep490.pilahub.dto.PersonalExerciseDto> exerciseDtoList = exercises
                                        .stream()
                                        .map(personalExerciseMapper::toDto)
                                        .collect(Collectors.toList());

                                return new RoadmapWithDetailsResponse.ScheduleWithDetails(
                                        personalScheduleMapper.toDto(schedule),
                                        exerciseDtoList);
                            })
                            .collect(Collectors.toList());

                    return new RoadmapWithDetailsResponse.StageWithDetails(
                            personalStageMapper.toDto(stage),
                            scheduleWithDetailsList);
                })
                .collect(Collectors.toList());

        return new RoadmapWithDetailsResponse(
                mapRoadmapDtoWithTotalAmount(roadmap),
                stageWithDetailsList);
    }

    // @Override
    // public List<RoadmapDto> getAllActive() {
    // return roadmapRepository.findByActiveTrue().stream()
    // .map(roadmapMapper::toDto)
    // .collect(Collectors.toList());
    // }

    @Override
    public List<RoadmapDto> searchByTitle(String title) {
        return roadmapRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapRoadmapDtoWithTotalAmount)
                .collect(Collectors.toList());
    }

    @Override
    public Page<RoadmapDto> getMyRoadmaps(fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapFilterRequest filter,
            org.springframework.data.domain.Pageable pageable) {
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();
        Role role = currentUser.getRole();

        log.info("Getting roadmaps for user {} with role {}", currentUserId, role);

        if (role == Role.TRAINEE) {
            Trainee trainee = traineeRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", currentUserId));
            return getRoadmapsWithFilters(trainee, null, filter, pageable);
        } else if (role == Role.COACH) {
            Coach coach = coachRepository.findById(currentUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coach", "id", currentUserId));
            return getRoadmapsWithFilters(null, coach, filter, pageable);
        } else {
            throw new IllegalStateException("Only trainees and coaches can access their roadmaps");
        }
    }

    @Override
    public RoadmapWithDetailsResponse getNewestRoadmapForTrainee() {
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();

        if (currentUser.getRole() != Role.TRAINEE) {
            throw new IllegalStateException("Only trainees can access their newest roadmap");
        }

        Trainee trainee = traineeRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", currentUserId));

        Roadmap roadmap = roadmapRepository
                .findFirstByTraineeAndStatusOrderByCreatedAtDesc(trainee, RoadmapStatus.IN_PROGRESS)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "trainee", currentUserId));

        return getRoadmapWithDetails(roadmap.getRoadmapId());
    }

    @Override
    public RoadmapWithDetailsResponse getPendingRoadmapForTrainee() {
        Account currentUser = securityUtil.getCurrentUser();
        UUID currentUserId = currentUser.getAccountId();

        if (currentUser.getRole() != Role.TRAINEE) {
            throw new IllegalStateException("Only trainees can access their pending roadmap");
        }

        Trainee trainee = traineeRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainee", "id", currentUserId));

        Roadmap roadmap = roadmapRepository
                .findFirstByTraineeAndStatusOrderByCreatedAtDesc(trainee, RoadmapStatus.PENDING)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "trainee", currentUserId));

        return getRoadmapWithDetails(roadmap.getRoadmapId());
    }

    /**
     * Helper method to get roadmaps with filters using JPA Specifications.
     */
    private Page<RoadmapDto> getRoadmapsWithFilters(Trainee trainee, Coach coach,
            fpt.edu.sep490.pilahub.dto.request.roadmap.RoadmapFilterRequest filter,
            org.springframework.data.domain.Pageable pageable) {

        org.springframework.data.jpa.domain.Specification<Roadmap> spec = (root, query, cb) -> cb.conjunction();

        // Filter by trainee
        if (trainee != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("trainee"), trainee));
        }

        // Filter by coach
        if (coach != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("coach"), coach));
        }

        // Apply additional filters if provided
        if (filter != null) {
            // Filter by title (partial match, case-insensitive)
            if (filter.title() != null && !filter.title().isBlank()) {
                spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("title")),
                        "%" + filter.title().toLowerCase() + "%"));
            }

            // Filter by status
            if (filter.status() != null) {
                spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), filter.status()));
            }

            // Filter by source
            if (filter.source() != null && !filter.source().isBlank()) {
                spec = spec.and((root, query, cb) -> cb.like(cb.lower(root.get("source")),
                        "%" + filter.source().toLowerCase() + "%"));
            }

            // Filter by start date range
            if (filter.startDateFrom() != null) {
                spec = spec.and(
                        (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("startDate"), filter.startDateFrom()));
            }
            if (filter.startDateTo() != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("startDate"), filter.startDateTo()));
            }

            // Filter by end date range
            if (filter.endDateFrom() != null) {
                spec = spec
                        .and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("endDate"), filter.endDateFrom()));
            }
            if (filter.endDateTo() != null) {
                spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("endDate"), filter.endDateTo()));
            }
        }

        return roadmapRepository.findAll(spec, pageable).map(this::mapRoadmapDtoWithTotalAmount);
    }

    @Override
    public RoadmapDto updateRoadmap(UUID roadmapId, UpdateRoadmapRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        if (request.title() != null) {
            roadmap.setTitle(request.title());
        }
        if (request.description() != null) {
            roadmap.setDescription(request.description());
        }
        if (request.startDate() != null) {
            roadmap.setStartDate(request.startDate());
        }
        if (request.endDate() != null) {
            roadmap.setEndDate(request.endDate());
        }
        if (request.source() != null) {
            roadmap.setSource(request.source());
        }

        // Update goals if primaryGoalId is provided
        if (request.primaryGoalId() != null) {
            // Clear existing goals
            roadmap.getRoadmapGoals().clear();
            // Save new goals
            saveRoadmapGoals(roadmap, request.primaryGoalId(), request.secondaryGoalIds());
        }

        return mapRoadmapDtoWithTotalAmount(roadmapRepository.save(roadmap));
    }

    @Override
    public RoadmapWithDetailsResponse resetProgressAndReschedule(UUID roadmapId, UpdateRoadmapScheduleRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        List<PersonalStage> stages = personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap);

        List<PersonalSchedule> allSchedules = new ArrayList<>();
        for (PersonalStage stage : stages) {
            allSchedules.addAll(personalScheduleRepository.findByPersonalStageOrderByScheduledDateAsc(stage));
        }

        List<Instant> generatedDates = generateScheduleDates(
                request.startDate(),
                request.trainingDays(),
                allSchedules.size());

        for (int i = 0; i < allSchedules.size(); i++) {
            PersonalSchedule schedule = allSchedules.get(i);
            Instant generatedDate = generatedDates.get(i);
            schedule.setScheduledDate(generatedDate);
            schedule.setDayOfWeek(LocalDate.ofInstant(generatedDate, VN_ZONE).getDayOfWeek().name());
            schedule.setCompleted(false);
            schedule.setCompletedAt(null);
        }
        personalScheduleRepository.saveAll(allSchedules);

        List<PersonalExercise> allExercises = new ArrayList<>();
        for (PersonalSchedule schedule : allSchedules) {
            allExercises.addAll(personalExerciseRepository.findByPersonalScheduleOrderByExerciseOrderAsc(schedule));
        }
        for (PersonalExercise exercise : allExercises) {
            exercise.setCompleted(false);
            exercise.setCompletedAt(null);
        }
        personalExerciseRepository.saveAll(allExercises);

        for (PersonalStage stage : stages) {
            stage.setCompleted(false);
            List<PersonalSchedule> stageSchedules = allSchedules.stream()
                    .filter(schedule -> schedule.getPersonalStage().getPersonalStageId()
                            .equals(stage.getPersonalStageId()))
                    .collect(Collectors.toList());
            if (!stageSchedules.isEmpty()) {
                Instant minStageDate = stageSchedules.stream()
                        .map(PersonalSchedule::getScheduledDate)
                        .filter(Objects::nonNull)
                        .min(Instant::compareTo)
                        .orElse(null);
                Instant maxStageDate = stageSchedules.stream()
                        .map(PersonalSchedule::getScheduledDate)
                        .filter(Objects::nonNull)
                        .max(Instant::compareTo)
                        .orElse(null);
                stage.setStartDate(minStageDate);
                stage.setEndDate(maxStageDate);
            }
        }
        personalStageRepository.saveAll(stages);

        roadmap.setProgressPercent(0);
        roadmap.setStartDate(request.startDate().atStartOfDay(VN_ZONE).toInstant());
        roadmap.setStatus(RoadmapStatus.IN_PROGRESS);
        if (!allSchedules.isEmpty()) {
            Instant maxScheduleDate = allSchedules.stream()
                    .map(PersonalSchedule::getScheduledDate)
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(null);
            roadmap.setEndDate(maxScheduleDate);
        }
        roadmapRepository.save(roadmap);

        return getRoadmapWithDetails(roadmapId);
    }

    @Override
    public RoadmapWithDetailsResponse rescheduleIncompleteSchedules(UUID roadmapId,
            UpdateRoadmapScheduleRequest request) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        List<PersonalStage> stages = personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap);

        List<PersonalSchedule> incompleteSchedules = new ArrayList<>();
        List<PersonalSchedule> allSchedules = new ArrayList<>();
        for (PersonalStage stage : stages) {
            List<PersonalSchedule> stageSchedules = personalScheduleRepository
                    .findByPersonalStageOrderByScheduledDateAsc(stage);
            allSchedules.addAll(stageSchedules);
            incompleteSchedules.addAll(stageSchedules.stream()
                    .filter(schedule -> !schedule.isCompleted())
                    .collect(Collectors.toList()));
        }

        if (incompleteSchedules.isEmpty()) {
            throw new IllegalStateException("No incomplete schedules found to reschedule");
        }

        List<Instant> generatedDates = generateScheduleDates(
                request.startDate(),
                request.trainingDays(),
                incompleteSchedules.size());

        for (int i = 0; i < incompleteSchedules.size(); i++) {
            PersonalSchedule schedule = incompleteSchedules.get(i);
            Instant generatedDate = generatedDates.get(i);
            schedule.setScheduledDate(generatedDate);
            schedule.setDayOfWeek(LocalDate.ofInstant(generatedDate, VN_ZONE).getDayOfWeek().name());
        }
        personalScheduleRepository.saveAll(incompleteSchedules);

        for (PersonalStage stage : stages) {
            List<PersonalSchedule> stageSchedules = allSchedules.stream()
                    .filter(schedule -> schedule.getPersonalStage().getPersonalStageId()
                            .equals(stage.getPersonalStageId()))
                    .collect(Collectors.toList());
            if (!stageSchedules.isEmpty()) {
                Instant minStageDate = stageSchedules.stream()
                        .map(PersonalSchedule::getScheduledDate)
                        .filter(Objects::nonNull)
                        .min(Instant::compareTo)
                        .orElse(null);
                Instant maxStageDate = stageSchedules.stream()
                        .map(PersonalSchedule::getScheduledDate)
                        .filter(Objects::nonNull)
                        .max(Instant::compareTo)
                        .orElse(null);
                stage.setStartDate(minStageDate);
                stage.setEndDate(maxStageDate);
            }
        }
        personalStageRepository.saveAll(stages);

        if (!allSchedules.isEmpty()) {
            Instant minScheduleDate = allSchedules.stream()
                    .map(PersonalSchedule::getScheduledDate)
                    .filter(Objects::nonNull)
                    .min(Instant::compareTo)
                    .orElse(null);
            Instant maxScheduleDate = allSchedules.stream()
                    .map(PersonalSchedule::getScheduledDate)
                    .filter(Objects::nonNull)
                    .max(Instant::compareTo)
                    .orElse(null);
            roadmap.setStartDate(minScheduleDate);
            roadmap.setEndDate(maxScheduleDate);
            roadmap.setStatus(RoadmapStatus.IN_PROGRESS);
            roadmapRepository.save(roadmap);
        }

        return getRoadmapWithDetails(roadmapId);
    }

    private List<Instant> generateScheduleDates(LocalDate startDate, List<DayOfWeek> trainingDays, int totalSchedules) {
        if (startDate == null) {
            throw new IllegalArgumentException("Start date must not be null");
        }
        if (trainingDays == null || trainingDays.isEmpty()) {
            throw new IllegalArgumentException("Training days list must not be empty");
        }

        List<Instant> scheduledDates = new ArrayList<>();
        Set<DayOfWeek> trainingDaySet = new HashSet<>(trainingDays);

        LocalDate currentDate = startDate;
        while (scheduledDates.size() < totalSchedules) {
            if (trainingDaySet.contains(currentDate.getDayOfWeek())) {
                scheduledDates.add(currentDate.atStartOfDay(VN_ZONE).toInstant());
            }
            currentDate = currentDate.plusDays(1);
        }

        return scheduledDates;
    }

    @Override
    public RoadmapDto updateProgress(UUID roadmapId, Integer progressPercent) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        if (progressPercent < 0 || progressPercent > 100) {
            throw new IllegalArgumentException("Progress percent must be between 0 and 100");
        }

        roadmap.setProgressPercent(progressPercent);

        // Update status and related objects when roadmap is completed
        if (progressPercent == 100) {
            updateStatusWhenProgressComplete(roadmap);
        }

        return mapRoadmapDtoWithTotalAmount(roadmapRepository.save(roadmap));
    }

    /**
     * Updates the roadmap status and marks all related stages, schedules, and
     * exercises
     * as completed when progress reaches 100%.
     * 
     * @param roadmap the roadmap that has reached 100% completion
     */
    private void updateStatusWhenProgressComplete(Roadmap roadmap) {
        Instant completionTime = Instant.now();

        // Update roadmap status to COMPLETED
        roadmap.setStatus(RoadmapStatus.COMPLETED);

        // Fetch all stages for this roadmap
        List<PersonalStage> stages = personalStageRepository.findByRoadmapOrderByStageOrderAsc(roadmap);

        // Mark all stages and their related schedules/exercises as completed
        for (PersonalStage stage : stages) {
            stage.setCompleted(true);

            // Fetch and mark all schedules in this stage as completed
            List<PersonalSchedule> schedules = personalScheduleRepository
                    .findByPersonalStageOrderByScheduledDateAsc(stage);

            for (PersonalSchedule schedule : schedules) {
                schedule.setCompleted(true);
                schedule.setCompletedAt(completionTime);

                // Fetch and mark all exercises in this schedule as completed
                List<PersonalExercise> exercises = personalExerciseRepository
                        .findByPersonalScheduleOrderByExerciseOrderAsc(schedule);

                for (PersonalExercise exercise : exercises) {
                    exercise.setCompleted(true);
                    exercise.setCompletedAt(completionTime);
                }

                personalExerciseRepository.saveAll(exercises);
            }

            personalScheduleRepository.saveAll(schedules);
        }

        personalStageRepository.saveAll(stages);

        log.info("Updated status to COMPLETED for roadmap: {} and all related objects", roadmap.getRoadmapId());
    }

    @Override
    public RoadmapDto updateFinalHealthProfile(UUID roadmapId, UUID finalHealthProfileId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        if (!Objects.equals(roadmap.getProgressPercent(), 100)) {
            throw new IllegalStateException("Final health profile can only be set when roadmap progress is 100%");
        }

        HealthProfile healthProfile = healthProfileRepository.findById(finalHealthProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("HealthProfile", "id", finalHealthProfileId));

        UUID roadmapTraineeId = roadmap.getTrainee() != null ? roadmap.getTrainee().getTraineeId() : null;
        UUID profileTraineeId = healthProfile.getTrainee() != null ? healthProfile.getTrainee().getTraineeId() : null;
        if (!Objects.equals(roadmapTraineeId, profileTraineeId)) {
            throw new IllegalArgumentException("Final health profile must belong to the same trainee as the roadmap");
        }

        roadmap.setFinalHealthProfileId(finalHealthProfileId);
        return mapRoadmapDtoWithTotalAmount(roadmapRepository.save(roadmap));
    }

    @Override
    public UUID getInitialHealthProfileId(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));
        return roadmap.getInitialHealthProfileId();
    }

    @Override
    public UUID getFinalHealthProfileId(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));
        return roadmap.getFinalHealthProfileId();
    }

    @Override
    public RoadmapDto approveRoadmap(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        // Validate that the roadmap is in PENDING status
        if (roadmap.getStatus() != RoadmapStatus.PENDING) {
            throw new IllegalStateException(
                    "Only roadmaps with PENDING status can be approved. Current status: " + roadmap.getStatus());
        }

        // Only trainees can approve roadmaps
        Role role = securityUtil.getCurrentUser().getRole();
        if (role != Role.TRAINEE) {
            throw new IllegalStateException("Only trainees can approve roadmaps");
        }

        roadmap.setStatus(RoadmapStatus.IN_PROGRESS);
        return mapRoadmapDtoWithTotalAmount(roadmapRepository.save(roadmap));
    }

    private RoadmapDto mapRoadmapDtoWithTotalAmount(Roadmap roadmap) {
        RoadmapDto dto = roadmapMapper.toDto(roadmap);
        BigDecimal totalAmount = calculateRoadmapTotalAmount(roadmap);

        return new RoadmapDto(
                dto.roadmapId(),
                dto.title(),
                dto.description(),
                dto.startDate(),
                dto.endDate(),
                dto.progressPercent(),
                dto.source(),
                dto.status(),
                dto.goals(),
                dto.traineeId(),
                dto.coachId(),
                dto.initialHealthProfileId(),
                dto.finalHealthProfileId(),
                totalAmount,
                dto.createdAt(),
                dto.updatedAt());
    }

    private BigDecimal calculateRoadmapTotalAmount(Roadmap roadmap) {
        if (roadmap.getCoach() == null || roadmap.getCoach().getPricePerHour() == null) {
            return null;
        }

        int totalSchedules = personalScheduleRepository.countTotalSchedulesInRoadmap(roadmap.getRoadmapId());
        BigDecimal hoursPerSlot = systemConfigService.getHoursPerSlot();

        return roadmap.getCoach().getPricePerHour()
                .multiply(hoursPerSlot)
                .multiply(BigDecimal.valueOf(totalSchedules));
    }

    @Override
    public void deactivateRoadmap(UUID roadmapId) {
        Roadmap roadmap = roadmapRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap", "id", roadmapId));

        // Check if coach is trying to cancel an IN_PROGRESS roadmap
        Role role = securityUtil.getCurrentUser().getRole();
        if (role == Role.COACH && roadmap.getStatus() == RoadmapStatus.IN_PROGRESS) {
            throw new IllegalStateException("Coaches are not allowed to cancel roadmaps with IN_PROGRESS status");
        }

        roadmap.setStatus(RoadmapStatus.CANCELLED);
        roadmapRepository.save(roadmap);
    }

    @Override
    public void deleteRoadmap(UUID roadmapId) {
        if (!roadmapRepository.existsById(roadmapId)) {
            throw new ResourceNotFoundException("Roadmap", "id", roadmapId);
        }
        roadmapRepository.deleteById(roadmapId);
    }
}

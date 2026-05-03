package fpt.edu.sep490.pilahub.service.implement;

import fpt.edu.sep490.pilahub.dto.TutorialDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateTutorialRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateTutorialRequest;
import fpt.edu.sep490.pilahub.enums.Role;
import fpt.edu.sep490.pilahub.enums.SubscriptionStatus;
import fpt.edu.sep490.pilahub.exception.AccessDeniedException;
import fpt.edu.sep490.pilahub.exception.ResourceNotFoundException;
import fpt.edu.sep490.pilahub.mapper.TutorialMapper;
import fpt.edu.sep490.pilahub.pojo.Exercise;
import fpt.edu.sep490.pilahub.pojo.Tutorial;
import fpt.edu.sep490.pilahub.repository.ExerciseRepository;
import fpt.edu.sep490.pilahub.repository.SubscriptionRepository;
import fpt.edu.sep490.pilahub.repository.TraineeCourseRepository;
import fpt.edu.sep490.pilahub.repository.TutorialRepository;
import fpt.edu.sep490.pilahub.service.TutorialService;
import fpt.edu.sep490.pilahub.util.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TutorialServiceImpl implements TutorialService {

    private final TutorialRepository tutorialRepository;
    private final ExerciseRepository exerciseRepository;
    private final TutorialMapper tutorialMapper;
    private final SecurityUtil securityUtil;
    private final SubscriptionRepository subscriptionRepository;
    private final TraineeCourseRepository traineeCourseRepository;

    @Override
    public TutorialDto createTutorial(CreateTutorialRequest request) {
        // Verify exercise exists
        Exercise exercise = exerciseRepository.findById(request.exerciseId())
                .orElseThrow(() -> new ResourceNotFoundException("Exercise", "id", request.exerciseId()));

        // Check if tutorial already exists for this exercise
        if (tutorialRepository.existsByExercise_ExerciseId(request.exerciseId())) {
            throw new IllegalArgumentException("Tutorial already exists for exercise with id: " + request.exerciseId());
        }

        Tutorial tutorial = tutorialMapper.toEntity(request);
        tutorial.setExercise(exercise);

        Tutorial saved = tutorialRepository.save(tutorial);
        return tutorialMapper.toDto(saved);
    }

    @Override
    public TutorialDto getById(UUID tutorialId) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial", "id", tutorialId));
        return tutorialMapper.toDto(tutorial);
    }

    @Override
    public TutorialDto getByExerciseId(UUID exerciseId, UUID courseId) {
        // if (courseId != null) {
        // if (!checkExerciseInTraineeCourse(exerciseId, courseId)) {
        // throw new AccessDeniedException("Bạn cần mua khóa học này hoặc đăng kí gói để
        // xem nội dung");
        // }
        // } else {
        // if (!checkSubscription()) {
        // throw new AccessDeniedException("Bạn cần đăng kí gói để xem nội dung");
        // }
        // }

        Tutorial tutorial = tutorialRepository.findByExercise_ExerciseId(exerciseId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial", "exerciseId", exerciseId));

        return tutorialMapper.toDto(tutorial);
    }

    @Override
    public TutorialDto updateTutorial(UUID tutorialId, UpdateTutorialRequest request) {
        Tutorial tutorial = tutorialRepository.findById(tutorialId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutorial", "id", tutorialId));

        tutorialMapper.updateEntityFromRequest(request, tutorial);

        Tutorial updated = tutorialRepository.save(tutorial);
        return tutorialMapper.toDto(updated);
    }

    @Override
    public void deleteTutorial(UUID tutorialId) {
        if (!tutorialRepository.existsById(tutorialId)) {
            throw new ResourceNotFoundException("Tutorial", "id", tutorialId);
        }
        tutorialRepository.deleteById(tutorialId);
    }

    @Override
    public void deleteByExerciseId(UUID exerciseId) {
        if (!tutorialRepository.existsByExercise_ExerciseId(exerciseId)) {
            throw new ResourceNotFoundException("Tutorial", "exerciseId", exerciseId);
        }
        tutorialRepository.deleteByExercise_ExerciseId(exerciseId);
    }

    private boolean checkSubscription() {
        if (securityUtil.getCurrentUserRole() != Role.TRAINEE) {
            return true;
        }
        UUID traineeId = securityUtil.getCurrentUserId();
        if (traineeId == null)
            return false;
        boolean checkActive = subscriptionRepository.existsByTrainee_TraineeIdAndStatus(securityUtil.getCurrentUserId(),
                SubscriptionStatus.ACTIVE);
        boolean checkUpgraded = subscriptionRepository
                .existsByTrainee_TraineeIdAndStatus(securityUtil.getCurrentUserId(), SubscriptionStatus.UPGRADED);
        if (checkActive || checkUpgraded) {
            return true;
        }
        return false;
    }

    private boolean checkExerciseInTraineeCourse(UUID exerciseId, UUID courseId) {
        if (securityUtil.getCurrentUserRole() != Role.TRAINEE) {
            return true;
        }
        UUID traineeId = securityUtil.getCurrentUserId();
        if (traineeId == null)
            return false;
        boolean checkEnrolled = traineeCourseRepository.existsByTrainee_TraineeIdAndCourse_CourseId(traineeId,
                courseId);
        boolean checkExerciseBelongCourse = exerciseRepository.existsCourseExercise(courseId, exerciseId);
        if (checkEnrolled && checkExerciseBelongCourse) {
            return true;
        }
        return false;
    }

}

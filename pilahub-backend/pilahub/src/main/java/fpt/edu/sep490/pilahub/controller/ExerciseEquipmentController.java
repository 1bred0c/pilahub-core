package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.ExerciseEquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateExerciseEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.ExerciseEquipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercise-equipment")
@RequiredArgsConstructor
@Tag(name = "Exercise Equipment", description = "Exercise equipment relationship management endpoints")
public class ExerciseEquipmentController {

    private final ExerciseEquipmentService exerciseEquipmentService;

    @PostMapping
    @Operation(summary = "Create exercise equipment relationship", description = "Create a new exercise equipment relationship")
    @ApiResponse(responseCode = "201", description = "Exercise equipment relationship created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "404", description = "Exercise or Equipment not found")
    @ApiResponse(responseCode = "409", description = "Exercise equipment relationship already exists")
    public ResponseEntity<APIResponse<ExerciseEquipmentDto>> createExerciseEquipment(
            @Valid @RequestBody CreateExerciseEquipmentRequest request) {
        ExerciseEquipmentDto exerciseEquipment = exerciseEquipmentService.createExerciseEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Exercise equipment relationship created successfully", exerciseEquipment));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get exercise equipment by ID", description = "Retrieve a specific exercise equipment relationship by its ID")
    @ApiResponse(responseCode = "200", description = "Exercise equipment relationship found")
    @ApiResponse(responseCode = "404", description = "Exercise equipment relationship not found")
    public ResponseEntity<APIResponse<ExerciseEquipmentDto>> getExerciseEquipmentById(@PathVariable UUID id) {
        ExerciseEquipmentDto exerciseEquipment = exerciseEquipmentService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Exercise equipment relationship retrieved successfully", exerciseEquipment));
    }

    @GetMapping("/exercise/{exerciseId}")
    @Operation(summary = "Get equipment by exercise", description = "Retrieve all equipment for a specific exercise")
    @ApiResponse(responseCode = "200", description = "Equipment retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseEquipmentDto>>> getEquipmentByExercise(@PathVariable UUID exerciseId) {
        List<ExerciseEquipmentDto> equipment = exerciseEquipmentService.getByExerciseId(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Equipment retrieved successfully", equipment));
    }

    @GetMapping("/exercise/{exerciseId}/required")
    @Operation(summary = "Get required equipment", description = "Retrieve all required equipment for a specific exercise")
    @ApiResponse(responseCode = "200", description = "Required equipment retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseEquipmentDto>>> getRequiredEquipmentByExercise(@PathVariable UUID exerciseId) {
        List<ExerciseEquipmentDto> equipment = exerciseEquipmentService.getRequiredByExerciseId(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Required equipment retrieved successfully", equipment));
    }

    @GetMapping("/exercise/{exerciseId}/alternatives")
    @Operation(summary = "Get alternative equipment", description = "Retrieve all alternative equipment for a specific exercise")
    @ApiResponse(responseCode = "200", description = "Alternative equipment retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseEquipmentDto>>> getAlternativeEquipmentByExercise(@PathVariable UUID exerciseId) {
        List<ExerciseEquipmentDto> equipment = exerciseEquipmentService.getAlternativesByExerciseId(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Alternative equipment retrieved successfully", equipment));
    }

    @GetMapping("/equipment/{equipmentId}")
    @Operation(summary = "Get exercises by equipment", description = "Retrieve all exercises that use a specific equipment")
    @ApiResponse(responseCode = "200", description = "Exercises retrieved successfully")
    public ResponseEntity<APIResponse<List<ExerciseEquipmentDto>>> getExercisesByEquipment(@PathVariable UUID equipmentId) {
        List<ExerciseEquipmentDto> exercises = exerciseEquipmentService.getByEquipmentId(equipmentId);
        return ResponseEntity.ok(APIResponse.success("Exercises retrieved successfully", exercises));
    }

    @GetMapping("/exists")
    @Operation(summary = "Check if relationship exists", description = "Check if an exercise equipment relationship exists")
    @ApiResponse(responseCode = "200", description = "Check completed")
    public ResponseEntity<APIResponse<Boolean>> checkExerciseEquipmentExists(
            @RequestParam UUID exerciseId,
            @RequestParam UUID equipmentId) {
        boolean exists = exerciseEquipmentService.existsByExerciseAndEquipment(exerciseId, equipmentId);
        return ResponseEntity.ok(APIResponse.success("Check completed", exists));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update exercise equipment relationship", description = "Update an existing exercise equipment relationship")
    @ApiResponse(responseCode = "200", description = "Exercise equipment relationship updated successfully")
    @ApiResponse(responseCode = "404", description = "Exercise equipment relationship not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<ExerciseEquipmentDto>> updateExerciseEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExerciseEquipmentRequest request) {
        ExerciseEquipmentDto exerciseEquipment = exerciseEquipmentService.updateExerciseEquipment(id, request);
        return ResponseEntity.ok(APIResponse.success("Exercise equipment relationship updated successfully", exerciseEquipment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete exercise equipment relationship", description = "Delete an exercise equipment relationship")
    @ApiResponse(responseCode = "200", description = "Exercise equipment relationship deleted successfully")
    @ApiResponse(responseCode = "404", description = "Exercise equipment relationship not found")
    public ResponseEntity<APIResponse<Void>> deleteExerciseEquipment(@PathVariable UUID id) {
        exerciseEquipmentService.deleteExerciseEquipment(id);
        return ResponseEntity.ok(APIResponse.success("Exercise equipment relationship deleted successfully", null));
    }

    @DeleteMapping("/exercise/{exerciseId}")
    @Operation(summary = "Delete by exercise", description = "Delete all equipment relationships for a specific exercise")
    @ApiResponse(responseCode = "200", description = "Exercise equipment relationships deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteByExercise(@PathVariable UUID exerciseId) {
        exerciseEquipmentService.deleteByExerciseId(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Exercise equipment relationships deleted successfully", null));
    }

    @DeleteMapping("/equipment/{equipmentId}")
    @Operation(summary = "Delete by equipment", description = "Delete all exercise relationships for a specific equipment")
    @ApiResponse(responseCode = "200", description = "Exercise equipment relationships deleted successfully")
    public ResponseEntity<APIResponse<Void>> deleteByEquipment(@PathVariable UUID equipmentId) {
        exerciseEquipmentService.deleteByEquipmentId(equipmentId);
        return ResponseEntity.ok(APIResponse.success("Exercise equipment relationships deleted successfully", null));
    }
}

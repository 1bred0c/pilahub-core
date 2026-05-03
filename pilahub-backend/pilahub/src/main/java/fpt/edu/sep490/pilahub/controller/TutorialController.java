package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.TutorialDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateTutorialRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateTutorialRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.TutorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tutorials")
@RequiredArgsConstructor
@Tag(name = "Tutorial", description = "Tutorial management endpoints")
public class TutorialController {

    private final TutorialService tutorialService;

    @PostMapping
    @Operation(summary = "Create tutorial", description = "Create a new tutorial for an exercise")
    @ApiResponse(responseCode = "201", description = "Tutorial created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<TutorialDto>> createTutorial(@Valid @RequestBody CreateTutorialRequest request) {
        TutorialDto tutorial = tutorialService.createTutorial(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Tutorial created successfully", tutorial));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get tutorial by ID", description = "Retrieve a specific tutorial by its ID")
    @ApiResponse(responseCode = "200", description = "Tutorial found")
    @ApiResponse(responseCode = "404", description = "Tutorial not found")
    public ResponseEntity<APIResponse<TutorialDto>> getTutorialById(@PathVariable UUID id) {
        TutorialDto tutorial = tutorialService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Tutorial retrieved successfully", tutorial));
    }

    @GetMapping("/exercise/{exerciseId}")
    @Operation(summary = "Get tutorial by exercise ID", description = "Retrieve a tutorial by its associated exercise ID")
    @ApiResponse(responseCode = "200", description = "Tutorial found")
    @ApiResponse(responseCode = "404", description = "Tutorial not found")
    public ResponseEntity<APIResponse<TutorialDto>> getTutorialByExerciseId(
            @PathVariable UUID exerciseId,
            @RequestParam(required = false) UUID courseId) {
        TutorialDto tutorial = tutorialService.getByExerciseId(exerciseId, courseId);
        return ResponseEntity.ok(APIResponse.success("Tutorial retrieved successfully", tutorial));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update tutorial", description = "Update an existing tutorial")
    @ApiResponse(responseCode = "200", description = "Tutorial updated successfully")
    @ApiResponse(responseCode = "404", description = "Tutorial not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<APIResponse<TutorialDto>> updateTutorial(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTutorialRequest request) {
        TutorialDto tutorial = tutorialService.updateTutorial(id, request);
        return ResponseEntity.ok(APIResponse.success("Tutorial updated successfully", tutorial));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete tutorial", description = "Permanently delete a tutorial")
    @ApiResponse(responseCode = "200", description = "Tutorial deleted successfully")
    @ApiResponse(responseCode = "404", description = "Tutorial not found")
    public ResponseEntity<APIResponse<Void>> deleteTutorial(@PathVariable UUID id) {
        tutorialService.deleteTutorial(id);
        return ResponseEntity.ok(APIResponse.success("Tutorial deleted successfully", null));
    }

    @DeleteMapping("/exercise/{exerciseId}")
    @Operation(summary = "Delete tutorial by exercise ID", description = "Delete tutorial associated with an exercise")
    @ApiResponse(responseCode = "200", description = "Tutorial deleted successfully")
    @ApiResponse(responseCode = "404", description = "Tutorial not found")
    public ResponseEntity<APIResponse<Void>> deleteTutorialByExerciseId(@PathVariable UUID exerciseId) {
        tutorialService.deleteByExerciseId(exerciseId);
        return ResponseEntity.ok(APIResponse.success("Tutorial deleted successfully", null));
    }
}

package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.BodyPartDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateBodyPartRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateBodyPartRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.service.BodyPartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/body-parts")
@RequiredArgsConstructor
@Tag(name = "Body Part", description = "Body part management endpoints")
public class BodyPartController {

    private final BodyPartService bodyPartService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Create body part", description = "Create a new body part")
    @ApiResponse(responseCode = "201", description = "Body part created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Body part with name already exists")
    public ResponseEntity<APIResponse<BodyPartDto>> createBodyPart(@Valid @RequestBody CreateBodyPartRequest request) {
        BodyPartDto bodyPart = bodyPartService.createBodyPart(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Body part created successfully", bodyPart));
    }

    @GetMapping
    @Operation(summary = "Get all body parts", description = "Retrieve all body parts")
    @ApiResponse(responseCode = "200", description = "Body parts retrieved successfully")
    public ResponseEntity<APIResponse<List<BodyPartDto>>> getAllBodyParts() {
        List<BodyPartDto> bodyParts = bodyPartService.getAll();
        return ResponseEntity.ok(APIResponse.success("Body parts retrieved successfully", bodyParts));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get body part by ID", description = "Retrieve a specific body part by its ID")
    @ApiResponse(responseCode = "200", description = "Body part found")
    @ApiResponse(responseCode = "404", description = "Body part not found")
    public ResponseEntity<APIResponse<BodyPartDto>> getBodyPartById(@PathVariable UUID id) {
        BodyPartDto bodyPart = bodyPartService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Body part retrieved successfully", bodyPart));
    }

    @GetMapping("/search")
    @Operation(summary = "Search body parts by name", description = "Search for body parts by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<BodyPartDto>>> searchBodyParts(@RequestParam String name) {
        List<BodyPartDto> bodyParts = bodyPartService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", bodyParts));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Update body part", description = "Update an existing body part")
    @ApiResponse(responseCode = "200", description = "Body part updated successfully")
    @ApiResponse(responseCode = "404", description = "Body part not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Body part with name already exists")
    public ResponseEntity<APIResponse<BodyPartDto>> updateBodyPart(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateBodyPartRequest request) {
        BodyPartDto bodyPart = bodyPartService.updateBodyPart(id, request);
        return ResponseEntity.ok(APIResponse.success("Body part updated successfully", bodyPart));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    @Operation(summary = "Delete body part", description = "Delete a body part")
    @ApiResponse(responseCode = "200", description = "Body part deleted successfully")
    @ApiResponse(responseCode = "404", description = "Body part not found")
    public ResponseEntity<APIResponse<Void>> deleteBodyPart(@PathVariable UUID id) {
        bodyPartService.deleteBodyPart(id);
        return ResponseEntity.ok(APIResponse.success("Body part deleted successfully", null));
    }
}


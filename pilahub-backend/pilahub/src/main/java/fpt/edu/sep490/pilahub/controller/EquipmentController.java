package fpt.edu.sep490.pilahub.controller;

import fpt.edu.sep490.pilahub.dto.EquipmentDto;
import fpt.edu.sep490.pilahub.dto.request.exercise.CreateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.request.exercise.UpdateEquipmentRequest;
import fpt.edu.sep490.pilahub.dto.response.APIResponse;
import fpt.edu.sep490.pilahub.dto.response.EquipmentRoadmapResponse;
import fpt.edu.sep490.pilahub.service.EquipmentService;
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
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
@Tag(name = "Equipment", description = "Equipment management endpoints")
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    @Operation(summary = "Create equipment", description = "Create a new equipment")
    @ApiResponse(responseCode = "201", description = "Equipment created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Equipment with name already exists")
    public ResponseEntity<APIResponse<EquipmentDto>> createEquipment(@Valid @RequestBody CreateEquipmentRequest request) {
        EquipmentDto equipment = equipmentService.createEquipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(APIResponse.success("Equipment created successfully", equipment));
    }

    @GetMapping()
    @Operation(summary = "Get all equipment", description = "Retrieve all of equipments")
    @ApiResponse(responseCode = "200", description = "Equipment found")
    @ApiResponse(responseCode = "404", description = "Equipment not found")
    public ResponseEntity<APIResponse<List<EquipmentDto>>> getAllEquipment() {
        List<EquipmentDto> equipments = equipmentService.findAll();
        return ResponseEntity.ok(APIResponse.success("Equipment retrieved successfully", equipments));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get equipment by ID", description = "Retrieve a specific equipment by its ID")
    @ApiResponse(responseCode = "200", description = "Equipment found")
    @ApiResponse(responseCode = "404", description = "Equipment not found")
    public ResponseEntity<APIResponse<EquipmentDto>> getEquipmentById(@PathVariable UUID id) {
        EquipmentDto equipment = equipmentService.getById(id);
        return ResponseEntity.ok(APIResponse.success("Equipment retrieved successfully", equipment));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get equipment by name", description = "Retrieve a specific equipment by its name")
    @ApiResponse(responseCode = "200", description = "Equipment found")
    @ApiResponse(responseCode = "404", description = "Equipment not found")
    public ResponseEntity<APIResponse<EquipmentDto>> getEquipmentByName(@PathVariable String name) {
        EquipmentDto equipment = equipmentService.getByName(name);
        return ResponseEntity.ok(APIResponse.success("Equipment retrieved successfully", equipment));
    }

    @GetMapping("/search")
    @Operation(summary = "Search equipment by name", description = "Search for equipment by name (case-insensitive)")
    @ApiResponse(responseCode = "200", description = "Search completed")
    public ResponseEntity<APIResponse<List<EquipmentDto>>> searchEquipment(@RequestParam String name) {
        List<EquipmentDto> equipment = equipmentService.searchByName(name);
        return ResponseEntity.ok(APIResponse.success("Search completed", equipment));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update equipment", description = "Update an existing equipment")
    @ApiResponse(responseCode = "200", description = "Equipment updated successfully")
    @ApiResponse(responseCode = "404", description = "Equipment not found")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "409", description = "Equipment with name already exists")
    public ResponseEntity<APIResponse<EquipmentDto>> updateEquipment(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateEquipmentRequest request) {
        EquipmentDto equipment = equipmentService.updateEquipment(id, request);
        return ResponseEntity.ok(APIResponse.success("Equipment updated successfully", equipment));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete equipment", description = "Delete an equipment")
    @ApiResponse(responseCode = "200", description = "Equipment deleted successfully")
    @ApiResponse(responseCode = "404", description = "Equipment not found")
    public ResponseEntity<APIResponse<Void>> deleteEquipment(@PathVariable UUID id) {
        equipmentService.deleteEquipment(id);
        return ResponseEntity.ok(APIResponse.success("Equipment deleted successfully", null));
    }

    @GetMapping("/roadmap/{roadmapId}")
    @Operation(summary = "Get equipment by roadmap", 
               description = "Retrieve all equipment required for a specific roadmap, aggregated with usage information")
    @ApiResponse(responseCode = "200", description = "Equipment retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Roadmap not found")
    public ResponseEntity<APIResponse<List<EquipmentRoadmapResponse>>> getEquipmentByRoadmap(@PathVariable UUID roadmapId) {
        List<EquipmentRoadmapResponse> equipment = equipmentService.getEquipmentByRoadmap(roadmapId);
        return ResponseEntity.ok(APIResponse.success("Equipment retrieved successfully", equipment));
    }
}
